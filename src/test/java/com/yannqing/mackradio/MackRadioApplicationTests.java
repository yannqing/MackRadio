package com.yannqing.mackradio;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.service.impl.VideoServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class MackRadioApplicationTests {

    @Resource
    private VideoService videoService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    String text = "灵渊大陆，东域，青云国。在青云国的边境，有一个不起眼的小镇，名为碧溪镇。镇上有一个少年，名叫云凡。他天生体弱，无法修炼，常受人白眼，但内心却怀有不凡的梦想——探索灵渊大陆的奥秘，成为传说中的灵尊。云凡的父亲是一位普通的铁匠，母亲早逝，留给他的只有一块看似普通的玉佩。然而，云凡十六岁生辰那日，玉佩突然发出耀眼的光芒，一股神秘力量涌入他的体内，彻底改变了他的命运。那一夜，云凡做了一个梦。梦中，他站在一片星辰璀璨的虚空之中，一位白发老者向他走来，声音虚无缥缈：“云凡，你的命运之轮已经开始转动，拿起你的剑，守护你心中的正义。”醒来后，云凡发现玉佩中蕴含着一部古老的修炼法门——《星辰变》。这部法门与众不同，它不依赖于外界的灵气，而是通过观想星辰，吸收宇宙之力。云凡开始按照《星辰变》的指引修炼，他的身体逐渐变得强健，灵气在体内流转，形成了一个微小的星核。随着修炼的深入，星核逐渐壮大，释放出强大的力量。碧溪镇外，有一片被称为妖兽森林的禁地。云凡为了检验自己的修炼成果，决定进入森林猎杀妖兽。在森林深处，他遇到了一只凶猛的火狼。火狼眼中闪烁着凶狠的光芒，向云凡扑来。云凡深吸一口气，调动体内的星辰之力，一拳轰出，拳风中带着星辰的轨迹，直接击中火狼的头部。火狼哀嚎一声，倒地不起。云凡上前，从火狼身上取出了一颗火红色的内丹。这次战斗，让云凡意识到了自己修炼法门的强大，也让他更加坚定了修炼的决心。随着实力的提升，云凡在碧溪镇的名声也逐渐响亮。他的行为引起了青云国大家族——萧家的注意。萧家家主萧天雄看中了云凡的潜力，决定收他为徒，带他进入更广阔的世界。云凡离开了碧溪镇，踏上了前往青云国都城的道路。在萧家，他接触到了更高级的修炼法门，结识了志同道合的伙伴，也见识了灵渊大陆的广阔与神秘。然而，随着实力的增长，云凡也逐渐卷入了灵渊大陆的纷争与斗争。他发现，自己的身世似乎与一个古老的预言有关，而这个预言，关乎着整个灵渊大陆的命运。";

    @Test
    void contextLoads() throws Exception {
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

        System.out.println(text.length());
    }


    /**
     * 根据文字生成图片---测试
     */
    @Test
    void createImage() {
//        List<String> picture = videoService.getPicture(text);
//        System.out.println();


    }

    @Resource
    private UserService userService;

    /**
     * 生成用户加密密码
     */
    @Test
    void createUserPassword() {
        String[] passwords = new String[] {
            "q1WADgiQzSiG", "X75MDTMCBZiE","GS1KLi4rBUjc","w8ZcSzMdu9aD","hDB2srMvmjg3","DOBDJzeUNMSM","gYT1yru5lBHj","hSBUZPlT0Lu7","ZgrNfvllNS33","XX8u9Yjjtk5s","v1spYpAn8cPf","DFgeDdsMEanr","IrzEO3NiqWIy","7xMIZPhXIg1z","gQuXcOgbEZfv"
        };

        List<String> pass = Arrays.asList(passwords);

        int index = 4;
        for (int i = 0; i < pass.size(); i++) {
            userService.update(new UpdateWrapper<User>().eq("id", index ++).set("password", passwordEncoder.encode(pass.get(i))));
        }
    }
}
