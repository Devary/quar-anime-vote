package com.votescroll.startup;

import com.votescroll.entity.*;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

    private static List<AnimeCharacter[]> seededShufflePairs(List<AnimeCharacter[]> input, int seed) {
        List<AnimeCharacter[]> arr = new ArrayList<>(input);
        int s = seed;
        for (int i = arr.size() - 1; i > 0; i--) {
            // Replicate JS Math.imul: plain Java int multiply truncates to 32 bits same as JS
            s = ((s ^ (s >>> 15)) * (s | 1)) ^ (((s ^ (s << 7)) * (s | 1)));
            s ^= (s >>> 16);
            int j = Integer.remainderUnsigned(s, i + 1);
            if (j < 0) j += (i + 1);
            AnimeCharacter[] tmp = arr.get(i); arr.set(i, arr.get(j)); arr.set(j, tmp);
        }
        return arr;
    }

    private void seedPolls() {
        Map<String, AnimeCharacter> charMap = new HashMap<>();
        AnimeCharacter.<AnimeCharacter>listAll().forEach(c -> charMap.put(c.id, c));
        List<AnimeCharacter> allChars = new ArrayList<>(charMap.values());

        // Generate all pairs
        List<AnimeCharacter[]> pairs = new ArrayList<>();
        for (int i = 0; i < allChars.size(); i++)
            for (int j = i + 1; j < allChars.size(); j++)
                pairs.add(new AnimeCharacter[]{allChars.get(i), allChars.get(j)});

        // Find and extract Imu vs Kaido as featured poll
        AnimeCharacter imu   = charMap.get("imu");
        AnimeCharacter kaido = charMap.get("kaido");
        AnimeCharacter[] featured = null;
        for (Iterator<AnimeCharacter[]> it = pairs.iterator(); it.hasNext(); ) {
            AnimeCharacter[] p = it.next();
            if ((p[0].id.equals("imu") && p[1].id.equals("kaido")) ||
                (p[0].id.equals("kaido") && p[1].id.equals("imu"))) {
                featured = new AnimeCharacter[]{imu, kaido};
                it.remove();
                break;
            }
        }

        // Shuffle with seed 1337
        List<AnimeCharacter[]> shuffled = seededShufflePairs(pairs, 1337);
        List<AnimeCharacter[]> selection = new ArrayList<>();
        if (featured != null) selection.add(featured);
        for (int i = 0; i < Math.min(149, shuffled.size()); i++) selection.add(shuffled.get(i));

        String[] questions = {
            "Who would win in a fight?","Who is the strongest?","Who is more powerful?",
            "Who is the greatest warrior?","Who would you side with?","Who has the superior power?",
            "Who would survive?","Who is more iconic?","Who is the fan favorite?",
            "Who is the better fighter?","Who would you want on your team?","Who is the bigger threat?",
            "Who has the better power set?","Who is the most feared?"
        };

        for (int i = 0; i < selection.size(); i++) {
            AnimeCharacter[] pair = selection.get(i);
            AnimeCharacter f1 = pair[0], f2 = pair[1];
            String[] sorted = {f1.id, f2.id};
            Arrays.sort(sorted);
            String id = sorted[0] + "-vs-" + sorted[1];
            String anime = f1.anime.equals(f2.anime) ? f1.anime : f1.anime + " x " + f2.anime;
            Poll poll = Poll.builder()
                .id(id).anime(anime).question(questions[i % questions.length])
                .fighter1(f1).fighter2(f2).build();
            poll.persist();
        }
        log.info("Seeded {} polls", Poll.count());
    }

    private void seedMultiPolls() {
        Map<String, AnimeCharacter> C = new HashMap<>();
        AnimeCharacter.<AnimeCharacter>listAll().forEach(c -> C.put(c.id, c));

        List<Object[]> defs = List.of(
            new Object[]{"mp-op-emperor","One Piece","Who is the strongest Pirate Emperor?", new Object[][]{
                {"Alliance",  new String[]{"luffy","shanks","whitebeard"}},
                {"Villains",  new String[]{"kaido","blackbeard","bigmom"}}
            }},
            new Object[]{"mp-naruto-greatest","Naruto","Who is the greatest shinobi of all time?", new Object[][]{
                {"Uchiha Clan",      new String[]{"madara","itachi","sasuke","obito"}},
                {"Senju & Uzumaki", new String[]{"naruto","minato","pain","hashirama"}}
            }},
            new Object[]{"mp-dbz-tournament","Dragon Ball Z","Who would win the Tournament of Power?", new Object[][]{
                {"Universe 7", new String[]{"goku","vegeta","gohan"}},
                {"Rivals",     new String[]{"jiren","broly","beerus"}}
            }},
            new Object[]{"mp-anime-goat","All Anime","Who is the GOAT anime protagonist?", new Object[][]{
                {"Big Three", new String[]{"luffy","naruto","ichigo"}},
                {"New Gen",   new String[]{"deku","eren","tanjiro"}}
            }},
            new Object[]{"mp-best-villain","All Anime","Who is the greatest anime villain ever?", new Object[][]{
                {"Classic Era", new String[]{"madara","aizen","frieza"}},
                {"Modern Era",  new String[]{"muzan","kaido","sukuna"}}
            }},
            new Object[]{"mp-hxh-best","Hunter x Hunter","Best Hunter x Hunter fighter?", new Object[][]{
                {"Heroes",   new String[]{"gon","killua","netero"}},
                {"Villains", new String[]{"hisoka","meruem","chrollo"}}
            }},
            new Object[]{"mp-bleach-clash","Bleach","Who reigns supreme in the Soul Society?", new Object[][]{
                {"Soul Reapers", new String[]{"ichigo","byakuya","zaraki"}},
                {"Enemies",      new String[]{"aizen","yhwach"}}
            }},
            new Object[]{"mp-aot-war","Attack on Titan","Who decides the fate of humanity?", new Object[][]{
                {"Survey Corps", new String[]{"eren","levi","mikasa","armin"}},
                {"Warriors",     new String[]{"zeke","reiner"}}
            }},
            new Object[]{"mp-jjk-sorcerers","Jujutsu Kaisen","Who is the ultimate cursed energy user?", new Object[][]{
                {"Special Grade",  new String[]{"gojo","yuta"}},
                {"Cursed Spirits", new String[]{"sukuna","megumi","yuji"}}
            }},
            new Object[]{"mp-ultimate-champion","All Anime","Who is the ultimate anime champion?", new Object[][]{
                {"Manga Kings", new String[]{"goku","luffy","naruto"}},
                {"Dark Lords",  new String[]{"madara","aizen","muzan"}},
                {"Wild Cards",  new String[]{"gojo","meruem","eren"}}
            }}
        );

        for (Object[] def : defs) {
            String id       = (String) def[0];
            String anime    = (String) def[1];
            String question = (String) def[2];
            Object[][] groupDefs = (Object[][]) def[3];

            MultiPoll mp = MultiPoll.builder().id(id).anime(anime).question(question).groups(new ArrayList<>()).build();
            mp.persist();

            for (int gi = 0; gi < groupDefs.length; gi++) {
                String label     = (String)   groupDefs[gi][0];
                String[] candIds = (String[]) groupDefs[gi][1];
                List<AnimeCharacter> candidates = Arrays.stream(candIds)
                    .map(C::get).filter(Objects::nonNull).collect(Collectors.toList());
                MultiPollGroup group = MultiPollGroup.builder()
                    .id(id + "-g" + gi).label(label).groupOrder(gi).poll(mp).candidates(candidates)
                    .build();
                group.persist();
                mp.groups.add(group);
            }
        }
        log.info("Seeded {} multi-polls", MultiPoll.count());
    }
}
