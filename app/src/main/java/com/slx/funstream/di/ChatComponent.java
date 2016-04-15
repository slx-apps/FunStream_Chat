package com.slx.funstream.di;

import com.slx.funstream.chat.ChatService;

import dagger.Component;

@PerChatService
@Component(
        modules = {
            ChatModule.class
        }, dependencies = {
            ApplicationComponent.class
        }
)
public interface ChatComponent extends ApplicationComponent {
    void inject(ChatService chatService);
}
