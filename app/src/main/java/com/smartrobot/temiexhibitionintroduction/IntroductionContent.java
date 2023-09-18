package com.smartrobot.temiexhibitionintroduction;

public interface IntroductionContent {
    String mg400_basic = "MG400是一台具備0.05mm重複精準度的桌上型工業機械手臂" +
            "，最大可乘載750克的負載，最大工作範圍可達440mm" +
            "，其設定簡單，可透過教導與再現的功能快速完成設定，適合用於小批量的自動化生產線" +
            "。其更可以透過各個程式語言，例如Python、Csharp的TCP、IP通訊功能" +
            "，根據設定好的通訊協議來撰寫程式控制MG400的動作";

//    String mg400_basic = "MG400最大可乘載750克的負載，最大工作範圍可達440mm";

    String cr5_basic = "CR5是一台具備0.02mm重複精準度的協作型工業機械手臂" +
            "，最大可乘載5公斤的負載，最大工作範圍可達900mm" +
            "，其設定容易，具備軌跡重現、安全皮膚碰撞感知、公規的末段點接口" +
            "，適合應用於各種不同產業的人機協作產現" +
            "。其更可以透過各個程式語言，例如Python、Csharp的TCP、IP通訊功能" +
            "，根據設定好的通訊協議來撰寫程式控制CR5的動作";

    String thouzer_basic = "Thouzer Basic是一台可應用於室內和戶外的自主導航載台" +
            "，其無須程式和網路設定，設定簡單" +
            "，短時間內就可以設定好要讓Thouzer自主導航的路徑" +
            "。其也具備追隨功能，其可以追隨人、台車和另一台Thouzer" +
            "。此外其外部構造為鋁擠型，可以容易裝載外部機構" +
            "，是一台可以靈活應用於各種環境的自主導航載台";

    String temi_basic = "temi是一台可以自主導航、避障、追隨以及人機互動的服務型機器人" +
            "，可以應用於迎賓帶位、送餐、遠端視訊等應用，其具備temi center網頁後台" +
            "，提供非程式化的控制介面，可以讓使用者簡單的編輯temi的任務和行為" +
            "，也具備Android SDK，使用者可以開發Android App來控制temi";

    String mg400_router = "目前正在展示的是讓Mg400夾取網路接頭" +
            "，不斷來回將網路接頭插入路由器的網路孔" +
            "，測試路由器的網路孔是否能正常讀取訊號" +
            "。過去這樣重複性的測試需要由人工完成" +
            "，但現在可以透過MG400來代替人力完成這樣重複性的測試";

    String mg400_tablet = "目前正在展示的是讓Mg400夾取觸控筆" +
            "，不斷在平板螢幕上透過觸控筆畫固定軌跡的線條" +
            "，藉此進行平板螢幕的觸控和穩定度測試" +
            "。過去這樣重複性的測試需要由人工完成" +
            "，但現在可以透過MG400來代替人力完成這樣重複性的測試" +
            "。此外您可以觀看平板右下角的xy座標值" +
            "，其代表的是目前觸控筆在平板上的位置" +
            "，可以透過座標值來驗證Mg400的精準度";

    String mg400_scale = "目前正在展示的是讓Mg400夾取示波器的探棒" +
            "，不斷在各個訊號產生器的接觸點上來回移動接觸" +
            "，測試示波器是否能夠讀取顯示正確的波型" +
            "。過去這樣重複性的測試需要由人工完成" +
            "，但現在可以透過MG400來代替人力完成這樣重複性的測試";

    String introduction_finish = "如果對此產品有興趣，歡迎您與現場的業務接洽" +
            "，如果需要看其他產品介紹請按下畫面上的按鈕讓我替您作介紹";

    String mg400_router_location = "路由器測試展示";
    String mg400_scale_location = "示波器測試展示";
    String mg400_tablet_location = "平板測試展示";
    String cr5_location = "協作手臂展示";
    String thouzer_location = "狗狗車展示";
    String temi_location = "服務機器人展示";
    String home_location = "home base";

    String temiMp4Path = "/sdcard/Download/temi_intro.mp4";
    String thouzerMp4Path = "/sdcard/Download/thouzer_intro.mp4";
    String cr5Mp4Path = "/sdcard/Download/cr5_intro.mp4";
    String mg400Mp4Path = "/sdcard/Download/mg400_intro.mp4";
}
