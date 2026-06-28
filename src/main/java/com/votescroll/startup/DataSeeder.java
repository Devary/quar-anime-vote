package com.votescroll.startup;

import com.votescroll.entity.*;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class DataSeeder {

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        if (AnimeCharacter.count() > 0) {
            log.info("DataSeeder: data already exists, skipping");
            return;
        }
        log.info("DataSeeder: seeding characters, polls and multi-polls...");
        seedCharacters();
        seedPolls();
        seedMultiPolls();
        log.info("DataSeeder: done. characters={}, polls={}, multiPolls={}",
            AnimeCharacter.count(), Poll.count(), MultiPoll.count());
    }

    private static String w(String wiki, String path) {
        return "https://static.wikia.nocookie.net/" + wiki + "/images/" + path;
    }

    private static String ph(String name, String hex) {
        String[] parts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) initials.append(Character.toUpperCase(p.charAt(0)));
        }
        String ini = initials.length() > 2 ? initials.substring(0, 2) : initials.toString();
        return "https://placehold.co/300x300/" + hex + "/ffffff?text=" + ini + "&font=montserrat";
    }

    private void seedCharacters() {
        List<AnimeCharacter> chars = List.of(
            // One Piece
            AnimeCharacter.builder().id("imu").name("Imu").title("Sovereign of the World").anime("One Piece").imageUrl(w("onepiece","2/2e/Im_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("kaido").name("Kaido").title("King of the Beasts").anime("One Piece").imageUrl(w("onepiece","9/9d/Kaido_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("luffy").name("Monkey D. Luffy").title("King of the Pirates").anime("One Piece").imageUrl(w("onepiece","6/6d/Monkey_D._Luffy_Anime_Post_Timeskip_Infobox.png")).build(),
            AnimeCharacter.builder().id("zoro").name("Roronoa Zoro").title("World's Greatest Swordsman").anime("One Piece").imageUrl(w("onepiece","3/35/Roronoa_Zoro_Anime_Post_Timeskip_Infobox.png")).build(),
            AnimeCharacter.builder().id("shanks").name("Shanks").title("Red-Haired Emperor").anime("One Piece").imageUrl(w("onepiece","d/da/Shanks_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("whitebeard").name("Whitebeard").title("World's Strongest Man").anime("One Piece").imageUrl(w("onepiece","e/e7/Edward_Newgate_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("ace").name("Portgas D. Ace").title("Fire Fist").anime("One Piece").imageUrl(w("onepiece","2/2c/Portgas_D._Ace_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("blackbeard").name("Blackbeard").title("Emperor of the Sea").anime("One Piece").imageUrl(w("onepiece","7/74/Marshall_D._Teach_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("doflamingo").name("Doflamingo").title("Heavenly Demon").anime("One Piece").imageUrl(w("onepiece","8/8f/Donquixote_Doflamingo_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("katakuri").name("Charlotte Katakuri").title("Sweet Commander").anime("One Piece").imageUrl(w("onepiece","4/4b/Charlotte_Katakuri_Anime_Infobox.png")).build(),
            AnimeCharacter.builder().id("bigmom").name("Big Mom").title("Empress of Totto Land").anime("One Piece").imageUrl(w("onepiece","0/0b/Charlotte_Linlin_Anime_Infobox.png")).build(),
            // Naruto
            AnimeCharacter.builder().id("naruto").name("Naruto Uzumaki").title("7th Hokage").anime("Naruto").imageUrl(w("naruto","d/d6/Naruto_newshot.png")).build(),
            AnimeCharacter.builder().id("sasuke").name("Sasuke Uchiha").title("Last Uchiha").anime("Naruto").imageUrl(w("naruto","2/21/Sasuke_newshot.png")).build(),
            AnimeCharacter.builder().id("madara").name("Madara Uchiha").title("God of Shinobi").anime("Naruto").imageUrl(w("naruto","3/35/Madara_Uchiha_Infobox.png")).build(),
            AnimeCharacter.builder().id("itachi").name("Itachi Uchiha").title("Prodigy of the Uchiha").anime("Naruto").imageUrl(w("naruto","b/bb/Itachi_Infobox.PNG")).build(),
            AnimeCharacter.builder().id("minato").name("Minato Namikaze").title("Yellow Flash").anime("Naruto").imageUrl(w("naruto","b/b4/Minato_Namikaze.png")).build(),
            AnimeCharacter.builder().id("pain").name("Pain").title("God of the Akatsuki").anime("Naruto").imageUrl(w("naruto","b/bf/Pain.png")).build(),
            AnimeCharacter.builder().id("obito").name("Obito Uchiha").title("Ten-Tails Jinchuriki").anime("Naruto").imageUrl(w("naruto","c/c5/Obito_newshot.png")).build(),
            AnimeCharacter.builder().id("hashirama").name("Hashirama Senju").title("God of Shinobi (1st)").anime("Naruto").imageUrl(ph("Hashirama","f97316")).build(),
            // Dragon Ball Z
            AnimeCharacter.builder().id("goku").name("Son Goku").title("Ultra Instinct").anime("Dragon Ball Z").imageUrl(w("dragonball","5/5b/Goku_DB_Manga_Infobox.png")).build(),
            AnimeCharacter.builder().id("vegeta").name("Vegeta").title("Prince of Saiyans").anime("Dragon Ball Z").imageUrl(w("dragonball","e/e4/Vegeta_Manga_Infobox.png")).build(),
            AnimeCharacter.builder().id("broly").name("Broly").title("Legendary Super Saiyan").anime("Dragon Ball Z").imageUrl(ph("Broly","4338ca")).build(),
            AnimeCharacter.builder().id("frieza").name("Frieza").title("Emperor of the Universe").anime("Dragon Ball Z").imageUrl(ph("Frieza","4338ca")).build(),
            AnimeCharacter.builder().id("beerus").name("Beerus").title("God of Destruction").anime("Dragon Ball Z").imageUrl(ph("Beerus","4338ca")).build(),
            AnimeCharacter.builder().id("jiren").name("Jiren").title("Pride Trooper").anime("Dragon Ball Z").imageUrl(ph("Jiren","4338ca")).build(),
            AnimeCharacter.builder().id("gohan").name("Gohan").title("Beast Gohan").anime("Dragon Ball Z").imageUrl(ph("Gohan","4338ca")).build(),
            AnimeCharacter.builder().id("cell").name("Cell").title("Perfect Form").anime("Dragon Ball Z").imageUrl(ph("Cell","4338ca")).build(),
            // Attack on Titan
            AnimeCharacter.builder().id("eren").name("Eren Yeager").title("The Founding Titan").anime("Attack on Titan").imageUrl(w("shingekinokyojin","e/ec/Eren_Yeager_%28Anime%29_character_image.png")).build(),
            AnimeCharacter.builder().id("levi").name("Levi Ackerman").title("Humanity's Strongest").anime("Attack on Titan").imageUrl(w("shingekinokyojin","4/4f/Levi_Ackerman_%28Anime%29_character_image.png")).build(),
            AnimeCharacter.builder().id("mikasa").name("Mikasa Ackerman").title("Ace Soldier").anime("Attack on Titan").imageUrl(w("shingekinokyojin","2/2b/Mikasa_Ackerman_%28Anime%29_character_image.png")).build(),
            AnimeCharacter.builder().id("zeke").name("Zeke Yeager").title("Beast Titan").anime("Attack on Titan").imageUrl(ph("Zeke","6b7280")).build(),
            AnimeCharacter.builder().id("armin").name("Armin Arlert").title("Commander of Survey Corps").anime("Attack on Titan").imageUrl(ph("Armin","6b7280")).build(),
            AnimeCharacter.builder().id("reiner").name("Reiner Braun").title("Armored Titan").anime("Attack on Titan").imageUrl(w("shingekinokyojin","a/a5/Reiner_Braun_character_image.png")).build(),
            // Demon Slayer
            AnimeCharacter.builder().id("tanjiro").name("Tanjiro Kamado").title("Sun Breathing Master").anime("Demon Slayer").imageUrl(w("kimetsu-no-yaiba","1/13/Tanjiro_anime_infobox.png")).build(),
            AnimeCharacter.builder().id("muzan").name("Muzan Kibutsuji").title("Demon King").anime("Demon Slayer").imageUrl(w("kimetsu-no-yaiba","3/3c/Muzan_Kibutsuji_anime_infobox.png")).build(),
            AnimeCharacter.builder().id("rengoku").name("Rengoku Kyojuro").title("Flame Hashira").anime("Demon Slayer").imageUrl(ph("Rengoku","dc2626")).build(),
            AnimeCharacter.builder().id("gyomei").name("Gyomei Himejima").title("Stone Hashira").anime("Demon Slayer").imageUrl(ph("Gyomei","dc2626")).build(),
            AnimeCharacter.builder().id("doma").name("Doma").title("Upper Moon Two").anime("Demon Slayer").imageUrl(ph("Doma","dc2626")).build(),
            AnimeCharacter.builder().id("kokushibo").name("Kokushibo").title("Upper Moon One").anime("Demon Slayer").imageUrl(ph("Kokushibo","dc2626")).build(),
            // Bleach
            AnimeCharacter.builder().id("ichigo").name("Ichigo Kurosaki").title("Substitute Soul Reaper").anime("Bleach").imageUrl(ph("Ichigo","7c3aed")).build(),
            AnimeCharacter.builder().id("aizen").name("Sosuke Aizen").title("Hogyoku Transcendent").anime("Bleach").imageUrl(ph("Aizen","7c3aed")).build(),
            AnimeCharacter.builder().id("byakuya").name("Byakuya Kuchiki").title("Head of Kuchiki Clan").anime("Bleach").imageUrl(ph("Byakuya","7c3aed")).build(),
            AnimeCharacter.builder().id("yhwach").name("Yhwach").title("King of Quincy").anime("Bleach").imageUrl(ph("Yhwach","7c3aed")).build(),
            AnimeCharacter.builder().id("zaraki").name("Kenpachi Zaraki").title("Captain of Squad 11").anime("Bleach").imageUrl(ph("Zaraki","7c3aed")).build(),
            // My Hero Academia
            AnimeCharacter.builder().id("allmight").name("All Might").title("Symbol of Peace").anime("My Hero Academia").imageUrl(ph("All Might","1d4ed8")).build(),
            AnimeCharacter.builder().id("deku").name("Izuku Midoriya").title("Deku").anime("My Hero Academia").imageUrl(ph("Deku","1d4ed8")).build(),
            AnimeCharacter.builder().id("bakugo").name("Katsuki Bakugo").title("Dynamight").anime("My Hero Academia").imageUrl(ph("Bakugo","1d4ed8")).build(),
            AnimeCharacter.builder().id("shigaraki").name("Tomura Shigaraki").title("League of Villains Leader").anime("My Hero Academia").imageUrl(ph("Shigaraki","1d4ed8")).build(),
            AnimeCharacter.builder().id("todoroki").name("Shoto Todoroki").title("Half-Cold Half-Hot").anime("My Hero Academia").imageUrl(ph("Todoroki","1d4ed8")).build(),
            // Jujutsu Kaisen
            AnimeCharacter.builder().id("gojo").name("Satoru Gojo").title("Strongest Sorcerer").anime("Jujutsu Kaisen").imageUrl(ph("Gojo","0f766e")).build(),
            AnimeCharacter.builder().id("sukuna").name("Ryomen Sukuna").title("King of Curses").anime("Jujutsu Kaisen").imageUrl(ph("Sukuna","0f766e")).build(),
            AnimeCharacter.builder().id("yuji").name("Yuji Itadori").title("Sukuna's Vessel").anime("Jujutsu Kaisen").imageUrl(ph("Yuji","0f766e")).build(),
            AnimeCharacter.builder().id("yuta").name("Yuta Okkotsu").title("Special Grade Sorcerer").anime("Jujutsu Kaisen").imageUrl(ph("Yuta","0f766e")).build(),
            AnimeCharacter.builder().id("megumi").name("Megumi Fushiguro").title("Ten Shadows Sorcerer").anime("Jujutsu Kaisen").imageUrl(ph("Megumi","0f766e")).build(),
            // Hunter x Hunter
            AnimeCharacter.builder().id("gon").name("Gon Freecss").title("Hunter").anime("Hunter x Hunter").imageUrl(ph("Gon","10b981")).build(),
            AnimeCharacter.builder().id("killua").name("Killua Zoldyck").title("Assassin Hunter").anime("Hunter x Hunter").imageUrl(ph("Killua","10b981")).build(),
            AnimeCharacter.builder().id("hisoka").name("Hisoka Morow").title("Magician").anime("Hunter x Hunter").imageUrl(ph("Hisoka","10b981")).build(),
            AnimeCharacter.builder().id("meruem").name("Meruem").title("King of Chimera Ants").anime("Hunter x Hunter").imageUrl(ph("Meruem","10b981")).build(),
            AnimeCharacter.builder().id("netero").name("Isaac Netero").title("Hunter Association Chairman").anime("Hunter x Hunter").imageUrl(ph("Netero","10b981")).build(),
            AnimeCharacter.builder().id("chrollo").name("Chrollo Lucilfer").title("Leader of Phantom Troupe").anime("Hunter x Hunter").imageUrl(ph("Chrollo","10b981")).build(),
            // Fullmetal Alchemist
            AnimeCharacter.builder().id("edward").name("Edward Elric").title("Fullmetal Alchemist").anime("Fullmetal Alchemist").imageUrl(ph("Edward","d97706")).build(),
            AnimeCharacter.builder().id("roy").name("Roy Mustang").title("Flame Alchemist").anime("Fullmetal Alchemist").imageUrl(ph("Roy","d97706")).build(),
            AnimeCharacter.builder().id("father").name("Father").title("Dwarf in the Flask").anime("Fullmetal Alchemist").imageUrl(ph("Father","d97706")).build(),
            AnimeCharacter.builder().id("scar").name("Scar").title("Ishvalan Warrior").anime("Fullmetal Alchemist").imageUrl(ph("Scar","d97706")).build()
        );
        for (AnimeCharacter c : chars) c.persist();
        log.info("Seeded {} characters", chars.size());
    }

    private void seedPolls() {
        Map<String, AnimeCharacter> C = new HashMap<>();
        AnimeCharacter.<AnimeCharacter>listAll().forEach(c -> C.put(c.id, c));

        record PollDef(String id, String anime, String question, String[] fighterIds) {}

        List<PollDef> defs = List.of(
            new PollDef("poll-op-top3", "One Piece",
                "Who is the strongest One Piece fighter?",
                new String[]{"luffy", "zoro", "shanks"}),
            new PollDef("poll-uchiha-best", "Naruto",
                "Who is the most powerful Uchiha?",
                new String[]{"madara", "itachi", "sasuke", "obito"}),
            new PollDef("poll-dbz-power", "Dragon Ball Z",
                "Who would win in a Dragon Ball ultimate showdown?",
                new String[]{"goku", "vegeta", "broly", "beerus", "jiren"}),
            new PollDef("poll-anime-goat", "All Anime",
                "Who is the greatest anime protagonist ever?",
                new String[]{"luffy", "naruto", "goku", "deku", "ichigo", "tanjiro", "yuji", "gon"}),
            new PollDef("poll-villain-throne", "All Anime",
                "Who sits on the villain's throne?",
                new String[]{"madara", "aizen", "muzan", "frieza", "meruem", "sukuna"})
        );

        for (PollDef def : defs) {
            List<AnimeCharacter> fighters = Arrays.stream(def.fighterIds())
                .map(C::get).filter(Objects::nonNull).collect(Collectors.toList());
            Poll poll = Poll.builder()
                .id(def.id()).anime(def.anime()).question(def.question())
                .fighter1(fighters.get(0))
                .fighter2(fighters.get(1))
                .fighters(fighters)
                .build();
            poll.persist();
        }
        log.info("Seeded {} polls", Poll.count());
    }

    private void seedMultiPolls() {
        Map<String, AnimeCharacter> C = new HashMap<>();
        AnimeCharacter.<AnimeCharacter>listAll().forEach(c -> C.put(c.id, c));

        Instant now = Instant.now();

        // startDay/endDay = days offset from now; groups are sequential with non-overlapping periods
        record GroupDef(String label, String[] charIds, long startDay, long endDay) {}
        record PollDef(String id, String anime, String question, GroupDef[] groups) {}

        List<PollDef> defs = List.of(
            new PollDef("mp-op-wano", "One Piece",
                "Who reigned supreme in the Wano arc?",
                new GroupDef[]{
                    new GroupDef("Straw Hats",   new String[]{"luffy", "zoro", "shanks"},          0,  30),
                    new GroupDef("Beast Pirates", new String[]{"kaido", "doflamingo", "katakuri"}, 31,  60)
                }),
            new PollDef("mp-naruto-generations", "Naruto",
                "Who is the strongest across all Naruto generations?",
                new GroupDef[]{
                    new GroupDef("Founding Era",   new String[]{"madara", "hashirama"},            0,  20),
                    new GroupDef("Akatsuki Era",   new String[]{"pain", "itachi", "obito"},       21,  45),
                    new GroupDef("New Generation", new String[]{"naruto", "sasuke", "minato"},    46,  75)
                }),
            new PollDef("mp-dbz-sagas", "Dragon Ball Z",
                "Who wins their saga's ultimate showdown?",
                new GroupDef[]{
                    new GroupDef("Frieza Saga", new String[]{"goku", "vegeta", "frieza"},         0,  21),
                    new GroupDef("Cell Games",  new String[]{"gohan", "cell"},                   22,  42),
                    new GroupDef("Tournament",  new String[]{"beerus", "jiren", "broly"},        43,  70)
                }),
            new PollDef("mp-jjk-vs-ds", "All Anime",
                "Sorcerers vs Demon Slayers — who dominates?",
                new GroupDef[]{
                    new GroupDef("Special Grade Sorcerers", new String[]{"gojo", "sukuna", "yuta", "yuji"},          0,  45),
                    new GroupDef("Demon Slayer Corps",      new String[]{"tanjiro", "rengoku", "gyomei", "muzan"},  46,  90)
                }),
            new PollDef("mp-bleach-aot", "All Anime",
                "Soul Reapers vs Titan Warriors",
                new GroupDef[]{
                    new GroupDef("Soul Society", new String[]{"ichigo", "aizen", "byakuya", "zaraki"}, 0,  30),
                    new GroupDef("Survey Corps", new String[]{"eren", "levi", "mikasa", "armin"},     31,  60)
                })
        );

        for (PollDef pd : defs) {
            MultiPoll mp = MultiPoll.builder()
                .id(pd.id()).anime(pd.anime()).question(pd.question())
                .groups(new ArrayList<>())
                .build();
            mp.persist();

            GroupDef[] groups = pd.groups();
            for (int gi = 0; gi < groups.length; gi++) {
                GroupDef gd = groups[gi];
                List<AnimeCharacter> candidates = Arrays.stream(gd.charIds())
                    .map(C::get).filter(Objects::nonNull).collect(Collectors.toList());
                MultiPollGroup group = MultiPollGroup.builder()
                    .id(pd.id() + "-g" + gi)
                    .label(gd.label())
                    .groupOrder(gi)
                    .poll(mp)
                    .candidates(candidates)
                    .startDate(now.plus(gd.startDay(), ChronoUnit.DAYS))
                    .endDate(now.plus(gd.endDay(),     ChronoUnit.DAYS))
                    .build();
                group.persist();
                mp.groups.add(group);
            }
        }
        log.info("Seeded {} multi-polls", MultiPoll.count());
        seedBracketMultiPoll();
        log.info("Seeded {} multi-polls (including bracket)", MultiPoll.count());
    }

    private void seedBracketMultiPoll() {
        Map<String, AnimeCharacter> C = new HashMap<>();
        AnimeCharacter.<AnimeCharacter>listAll().forEach(c -> C.put(c.id, c));

        Instant now = Instant.now();

        MultiPoll mp = MultiPoll.builder()
            .id("mp-anime-tournament")
            .anime("All Anime")
            .question("Anime Grand Tournament — who is the ultimate champion?")
            .groups(new ArrayList<>())
            .build();
        mp.persist();

        // ── Level 0 : 4 quarter-final groups ──────────────────────────────────
        // IDs are fixed so feeder references below can use them directly
        String g0 = "mp-tournament-qf1"; // Blade Masters
        String g1 = "mp-tournament-qf2"; // Speed Demons
        String g2 = "mp-tournament-qf3"; // Dark Lords
        String g3 = "mp-tournament-qf4"; // Power Giants

        MultiPollGroup qf1 = MultiPollGroup.builder()
            .id(g0).label("Blade Masters").groupOrder(0).level(0).poll(mp)
            .candidates(chars(C, "zoro", "ichigo", "levi"))
            .feederGroupIds(new ArrayList<>())
            .startDate(now).endDate(now.plus(7, ChronoUnit.DAYS))
            .build();

        MultiPollGroup qf2 = MultiPollGroup.builder()
            .id(g1).label("Speed Demons").groupOrder(1).level(0).poll(mp)
            .candidates(chars(C, "minato", "killua", "goku"))
            .feederGroupIds(new ArrayList<>())
            .startDate(now).endDate(now.plus(7, ChronoUnit.DAYS))
            .build();

        MultiPollGroup qf3 = MultiPollGroup.builder()
            .id(g2).label("Dark Lords").groupOrder(2).level(0).poll(mp)
            .candidates(chars(C, "madara", "aizen", "muzan"))
            .feederGroupIds(new ArrayList<>())
            .startDate(now.plus(8, ChronoUnit.DAYS))
            .endDate(now.plus(14, ChronoUnit.DAYS))
            .build();

        MultiPollGroup qf4 = MultiPollGroup.builder()
            .id(g3).label("Power Giants").groupOrder(3).level(0).poll(mp)
            .candidates(chars(C, "kaido", "meruem", "whitebeard"))
            .feederGroupIds(new ArrayList<>())
            .startDate(now.plus(8, ChronoUnit.DAYS))
            .endDate(now.plus(14, ChronoUnit.DAYS))
            .build();

        // ── Level 1 : 2 semi-final groups ────────────────────────────────────
        String sf1id = "mp-tournament-sf1";
        String sf2id = "mp-tournament-sf2";

        MultiPollGroup sf1 = MultiPollGroup.builder()
            .id(sf1id).label("Semi-Final A").groupOrder(4).level(1).poll(mp)
            .candidates(new ArrayList<>()) // populated when QF1 & QF2 end
            .feederGroupIds(new ArrayList<>(List.of(g0, g1)))
            .startDate(now.plus(15, ChronoUnit.DAYS))
            .endDate(now.plus(21, ChronoUnit.DAYS))
            .build();

        MultiPollGroup sf2 = MultiPollGroup.builder()
            .id(sf2id).label("Semi-Final B").groupOrder(5).level(1).poll(mp)
            .candidates(new ArrayList<>()) // populated when QF3 & QF4 end
            .feederGroupIds(new ArrayList<>(List.of(g2, g3)))
            .startDate(now.plus(15, ChronoUnit.DAYS))
            .endDate(now.plus(21, ChronoUnit.DAYS))
            .build();

        // ── Level 2 : Grand Final ─────────────────────────────────────────────
        MultiPollGroup grand = MultiPollGroup.builder()
            .id("mp-tournament-final").label("Grand Final").groupOrder(6).level(2).poll(mp)
            .candidates(new ArrayList<>()) // populated when SF1 & SF2 end
            .feederGroupIds(new ArrayList<>(List.of(sf1id, sf2id)))
            .startDate(now.plus(22, ChronoUnit.DAYS))
            .endDate(now.plus(30, ChronoUnit.DAYS))
            .build();

        for (MultiPollGroup g : List.of(qf1, qf2, qf3, qf4, sf1, sf2, grand)) {
            g.persist();
            mp.groups.add(g);
        }
    }

    private List<AnimeCharacter> chars(Map<String, AnimeCharacter> C, String... ids) {
        return Arrays.stream(ids).map(C::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
