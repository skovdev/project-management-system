package local.pms.aiservice.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatGptModel {

    GPT_4_1("gpt-4.1-2025-04-14"),
    GPT_4_1_MINI("gpt-4.1-mini-2025-04-14");

    private final String name;

}
