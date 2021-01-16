package pl.chatme.web.rest.vm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: In this moment return te simple object for testing purpose
@Getter
@Setter
@NoArgsConstructor
public class SimpleMessageVM {
    private String from;
    private String content;

    public SimpleMessageVM(String from, String content) {
        this.from = from;
        this.content = content;
    }
}
