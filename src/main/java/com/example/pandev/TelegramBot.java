package com.example.pandev;

import com.example.pandev.Config.BotConfig;
import com.example.pandev.Config.ExchangeRateAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final ExchangeRateAPI exchangeRateAPI = new ExchangeRateAPI();
    final BotConfig config;
    String convertFrom;
    String convertTo;
    static final String HELP_TEXT = "This bot is created to convert currency, made for PanDev intership task\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /select to select currency you want to convet from\n\n" +
            "Type /help to see this message again";

    static final String ERROR_TEXT = "Error occurred: ";
    static final String KZT = "Tenge";
    static final String USD = "Dollar";
    BigDecimal quantity;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/select", "select currency you need to exchange"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));

        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

                switch (messageText) {
                    case "/start":


                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());

                        break;

                    case "/help":

                        sendMessage(chatId, HELP_TEXT);
                        break;

                    case "/select":
                        selectMessage(chatId);

                        break;


                    default:
                        System.out.println(Double.parseDouble(update.getMessage().getText()));
                        try {

                            if (!(convertFrom == null) && ((Double.valueOf(update.getMessage().getText())) instanceof Double)) {
                                quantity = BigDecimal.valueOf(Double.valueOf(update.getMessage().getText()));
                                BigDecimal result = exchangeRateAPI.convert(convertFrom,convertTo,quantity);
                                sendMessage(chatId,result+" "+convertTo);
                            }else if(convertFrom==null){
                                sendMessage(chatId,"Please use /select to select currency you want to convert from or use /help ");
                            }
                        }catch (NumberFormatException | IOException e){
                            sendMessage(chatId,"Please write only amount of money you need to convert");
                        }

                }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("KZT")) {
                String text = "You choose Tenge, now please write down amount of tenge you want to transfer to USD";
                convertFrom = "KZT";
                convertTo="USD";
            sendMessage(chatId,text);
            } else if (callbackData.equals("USD")) {
                String text = "You choose Dollar, now please write down amount of dollars you want to transfer to KZT";
                convertFrom = "USD";
                convertTo="KZT";
                sendMessage(chatId,text);
            }


        }
    }
    private void startCommandReceived(long chatId, String name) {


        String answer = ("Hi, " + name + ", nice to meet you!" );
        log.info("Replied to user " + name);


        sendMessage(chatId, answer);
    }
    private void selectMessage(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("please choose the currency you want to change from");
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        InlineKeyboardButton tenge = new InlineKeyboardButton();
        InlineKeyboardButton usd = new InlineKeyboardButton();
        tenge.setText(KZT);tenge.setCallbackData("KZT");

        usd.setText(USD);usd.setCallbackData("USD");
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowInline.add(tenge);rowInline.add(usd);
        rowsInline.add(rowInline);
        kb.setKeyboard(rowsInline);
        message.setReplyMarkup(kb);

        executeMessage(message);

    }
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);



        executeMessage(message);
    }


    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }



}
