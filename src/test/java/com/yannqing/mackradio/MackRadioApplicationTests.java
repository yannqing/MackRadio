package com.yannqing.mackradio;

import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.service.impl.VideoServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@SpringBootTest
class MackRadioApplicationTests {

    @Resource
    private VideoService videoService;

    @Test
    void contextLoads() throws Exception {
//        String text = "在无尽宇宙的深处，有一个名为灵渊的神秘世界。这里，天地灵气浓郁，万物皆有灵性，人类通过修炼灵气，可以开脉通络，掌握超凡的力量。\n" +
//                "灵渊大陆，东域，青云国。\n" +
//                "在青云国的边境，有一个不起眼的小镇，名为碧溪镇。镇上有一个少年，名叫云凡。他天生体弱，无法修炼，常受人白眼，但内心却怀有不凡的梦想——探索灵渊大陆的奥秘，成为传说中的灵尊。\n" +
//                "云凡的父亲是一位普通的铁匠，母亲早逝，留给他的只有一块看似普通的玉佩。然而，云凡十六岁生辰那日，玉佩突然发出耀眼的光芒，一股神秘力量涌入他的体内，彻底改变了他的命运。\n" +
//                "那一夜，云凡做了一个梦。梦中，他站在一片星辰璀璨的虚空之中，一位白发老者向他走来，声音虚无缥缈：“云凡，你的命运之轮已经开始转动，拿起你的剑，守护你心中的正义。”\n" +
//                "醒来后，云凡发现玉佩中蕴含着一部古老的修炼法门——《星辰变》。这部法门与众不同，它不依赖于外界的灵气，而是通过观想星辰，吸收宇宙之力。\n" +
//                "云凡开始按照《星辰变》的指引修炼，他的身体逐渐变得强健，灵气在体内流转，形成了一个微小的星核。随着修炼的深入，星核逐渐壮大，释放出强大的力量。\n" +
//                "碧溪镇外，有一片被称为妖兽森林的禁地。云凡为了检验自己的修炼成果，决定进入森林猎杀妖兽。在森林深处，他遇到了一只凶猛的火狼。火狼眼中闪烁着凶狠的光芒，向云凡扑来。\n" +
//                "云凡深吸一口气，调动体内的星辰之力，一拳轰出，拳风中带着星辰的轨迹，直接击中火狼的头部。火狼哀嚎一声，倒地不起。云凡上前，从火狼身上取出了一颗火红色的内丹。\n" +
//                "这次战斗，让云凡意识到了自己修炼法门的强大，也让他更加坚定了修炼的决心。\n" +
//                "随着实力的提升，云凡在碧溪镇的名声也逐渐响亮。他的行为引起了青云国大家族——萧家的注意。萧家家主萧天雄看中了云凡的潜力，决定收他为徒，带他进入更广阔的世界。\n" +
//                "云凡离开了碧溪镇，踏上了前往青云国都城的道路。在萧家，他接触到了更高级的修炼法门，结识了志同道合的伙伴，也见识了灵渊大陆的广阔与神秘。\n" +
//                "然而，随着实力的增长，云凡也逐渐卷入了灵渊大陆的纷争与斗争。他发现，自己的身世似乎与一个古老的预言有关，而这个预言，关乎着整个灵渊大陆的命运。\n" +
//                "在萧家修炼的日子里，云凡不断突破自我，从一个无法修炼的少年，成长为家族中的佼佼者。他的名字，开始在青云国乃至整个东域传开。\n" +
//                "一次偶然的机会，云凡在萧家的古老典籍中，发现了关于星辰之巅的记载。星辰之巅，是灵渊大陆的最高峰，传说那里是通往宇宙深处的门户，也是成就灵尊的必经之路。\n" +
//                "云凡心中涌起了一股强烈的渴望，他想要攀登星辰之巅，探索宇宙的奥秘，寻找自己真正的命运。\n" +
//                "随着云凡踏上前往星辰之巅的旅程，一个宏大的故事缓缓展开。在这条路上，他将面对无数的挑战和试炼，也将结识各种各样的伙伴和对手。而他最终能否成就灵尊，守护灵渊大陆的和平，一切都还是未知数。";
//        String outputPath = "C:\\Users\\67121\\video\\" + UUID.randomUUID().toString() + ".mp4";
//        videoService.change(text,
//                "C:\\Users\\67121\\video\\srt\\dialog.srt",
//                "C:\\Users\\67121\\video\\music\\240706235417374326643157.wav",
//                "C:\\Users\\67121\\video\\video\\output.mp4",
//                "C:\\Users\\67121\\video\\main.sh",
//                outputPath,
//                "C:\\Users\\67121\\video\\image\\"
//                );
//        videoService.getMp42(text);
    }
}
