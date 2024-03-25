package crawler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class URLUtilTest {
    @Test
    void urlHostNameTest() {
        String[][] testCases = {
                {
                        "https://travel.yahoo.co.jp/?ikCo=y_010017&sc_e=ytmh",
                        "travel.yahoo.co.jp"
                },
                {
                        "https://www.yahoo.co.jp/",
                        "www.yahoo.co.jp"
                },
                {
                        "https://login.yahoo.co.jp/config/login?.src=ym&req_attr=ym&.done=https%3A%2F%2Fmail.yahoo.co.jp%2F",
                        "login.yahoo.co.jp"
                },
                {
                        "http://login.yahoo.co.jp/config/login?.src=ym&req_attr=ym&.done=https%3A%2F%2Fmail.yahoo.co.jp%2F",
                        "login.yahoo.co.jp"
                },
        };
        for (String[] pair : testCases) {
            assertEquals(pair[1], URLUtil.getHostName(pair[0]));
        }
    }

    // @Test
    // void convertURLtoPathStrTest() {
    //     String[][] testCases = {
    //             {
    //                     "https://travel.yahoo.co.jp/?ikCo=y_010017&sc_e=ytmh",
    //                     "travel.yahoo.co.jp/index.html"
    //             },
    //             {
    //                     "https://www.yahoo.co.jp/",
    //                     "www.yahoo.co.jp/index.html"
    //             },
    //             {
    //                     "https://login.yahoo.co.jp/config/login?.src=ym&req_attr=ym&.done=https%3A%2F%2Fmail.yahoo.co.jp%2F",
    //                     "login.yahoo.co.jp/config/login"
    //             },
    //     };
    //     for (String[] pair : testCases) {
    //         assertEquals(pair[1], URLUtil.convertURLtoPathStr(pair[0]));
    //     }
    // }

    @Test
    void getURLExtTest() {
        String[][] testCases = {
                {
                        "https://travel.yahoo.co.jp/?ikCo=y_010017&sc_e=ytmh",
                        ""
                },
                {
                        "https://www.yahoo.co.jp/",
                        ""
                },
                {
                        "https://news-pctr.c.yimg.jp/t/news-topics/images/tpc/2024/2/29/c19917aaf466a387c1d525e1975ac1b90088e9e79f00d92be73e2fe56f239ed4.jpg?h=200&w=200&pri=l&fmt=webp",
                        ".jpg"
                },
                {
                        "https://search.yahoo.co.jp/realtime",
                        ""
                },
        };
        for (String[] pair : testCases) {
            assertEquals(pair[1], URLUtil.getURLExt(pair[0]));
        }
    }
}
