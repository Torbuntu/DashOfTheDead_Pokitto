import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.sound.Mixer;
import femto.font.TIC80;

import enemies.Ghoul;
import enemies.Bat;
import enemies.Spikes;
import enemies.Vampyre;

import backgrounds.Door;
import backgrounds.DungeonBackground;
import backgrounds.FrontBackground;
import backgrounds.Platform;
import backgrounds.TreeA;
import backgrounds.TreeB;
import backgrounds.TreeC;
import backgrounds.CarpetA;
import backgrounds.CarpetB;
import backgrounds.Rockway;
import backgrounds.Wood;

import skins.Tor;
import skins.WizardHat;
import skins.FishBowl;
import skins.Hero;
import skins.Tintitto;
import skins.Robot;
import skins.Fireman;

import audio.BatSplat;
import audio.CollectCoin;
import audio.Die;
import audio.Explode;
import audio.Hurt;
import audio.Jump;
import audio.PowerUp;
import audio.Splat;
import audio.Swap;

class CookieManager extends femto.Cookie {
    CookieManager() {
        super();
        begin("DOTDC");
    }
    boolean hasWizHat;
    boolean hasFishBowl;
    boolean hasHero;
    boolean hasTin;
    boolean hasRobo;
    boolean hasFire;
    
    int distScore;
    int killScore;
    int coins;
}

class Main extends State {

    static final var cookieManager = new CookieManager();

    HiRes16Color screen; // the screenmode we want to draw with
    //sounds
    Explode explode;
    Jump jumpSound;
    Hurt hurt;
    PowerUp powerUp;
    CollectCoin coinCollect;
    Die dieSound;
    Splat splat;
    BatSplat batSplat;
    Swap swap;
    //end sounds

    int stateManager = 0; //0 title, 1 pre-run, 2 game run, 3 dead

    Vampyre vamp = new Vampyre();
    int zapTime;
    boolean vampDead = false;

    boolean newHighScore = false;
    boolean jump = false, dashing = false, dashReady = true, ghoulDead = false, intro = true, coinIntro = true, doorshut = true, inDungeon = true, dead = false;
    boolean resetData = false; //false = Game Start, true = Reset Data
    boolean transIn = false, transOut = false;
    Tor tor;
    Coffee coffee;
    Ghoul ghoul;
    Bat[] bats;
    Spikes spike;
    boolean[] batDead;
    Coin[] coins;
    Coin coinIcon;
    Lock lock;
    Treadmill treadmill;

    //VANITY
    int vanity = 0;
    WizardHat wizHat;
    FishBowl fishBowl;
    Hero hero;
    Tintitto tintitto;
    Robot robot;
    Fireman fireman;

    //end vanity

    //Backgrounds
    CarpetA carpetA;
    CarpetB carpetB;
    int cpax;
    int cpbx;

    Rockway rockway;
    int rockwayX;
    
    Wood wood;

    Door door;
    DungeonBackground dungeonBackground;
    FrontBackground frontBackground;

    Platform platform;
    //END Backgrounds

    float dxs, dys, torGravity;
    int dashTime, speed, powerReady = 1, torJump, dgnX, torHurt, distance, kills;
    float t, shake, doorShake;
    int difficulty, nextDifficulty, cooldown, dashCharge = 50, torMaxJump = 1, torHits = 3;

    static int torCoins;
    //prestart screen variables
    int cursor, shopCharge = 50, shopJump = 1, shopHits = 3;
    boolean tailor = false;
    String message = "", tipText = "";
    String[] tips;
    int sbx = 0;
    TreeA treeA;
    TreeB treeB;
    TreeC treeC;

    int tax = 47, tbx = 108, tcx = 209;

    //title screen variables
    Title titleImage = new Title();

    public static void main(String[] args) {
        Mixer.init(8000);
        Game.run(TIC80.font(), new Main());
    }

    void init() {
        //Sounds
        explode = new Explode(0);
        jumpSound = new Jump(0);
        swap = new Swap(0);

        hurt = new Hurt(1);
        splat = new Splat(1);
        batSplat = new BatSplat(1);

        powerUp = new PowerUp(2);
        coinCollect = new CollectCoin(2);

        dieSound = new Die(3);
        //END Sounds

        screen = new HiRes16Color(JavaDashPalette.palette(), TIC80.font());
        cursor = 0;

        torCoins = 0;

        tips = new String[8];
        tips[0] = "Try dashing the middle of doors";
        tips[1] = "Dashing enemies will give you\nbonus coins";
        tips[2] = "Make sure to watch your power meter";
        tips[3] = "Time your powerups right\nto get farther";
        tips[4] = "Try purchasing powerups\nto get a head start";
        tips[5] = "Careful, you can't dash\nwhile hurt";
        tips[6] = "Do you have all the\nvanity suits?";
        tips[7] = "Hold down to drop through\nplatforms and fall quicker";

        //VANITY 
        tor = new Tor();
        wizHat = new WizardHat();
        fishBowl = new FishBowl();
        hero = new Hero();
        tintitto = new Tintitto();
        robot = new Robot();
        fireman = new Fireman();

        lock = new Lock();
        treadmill = new Treadmill();
        treadmill.idle();

        //END Vanity

        ghoul = new Ghoul();

        frontBackground = new FrontBackground();
        dungeonBackground = new DungeonBackground();
        rockway = new Rockway();
        treeA = new TreeA();
        treeA.idle();
        treeA.y = 129 - treeA.height();
        wood = new Wood();

        carpetA = new CarpetA();

        carpetB = new CarpetB();

        treeB = new TreeB();
        treeC = new TreeC();

        door = new Door();
        door.x = 220;

        platform = new Platform();
        platform.idle();

        coffee = new Coffee();
        coffee.idle();

        coinIcon = new Coin();
        coinIcon.coin();
        resetSkins();
        subInit();
        stateManager = 0;
    }

    void subInit() {

        difficulty = 1;
        nextDifficulty = 500;
        distance = 0;
        powerReady = -1;
        kills = 0;
        cooldown = 75;
        dgnX = 0;
        rockwayX = -12;

        dashTime = dashCharge;
        speed = 1;

        torGravity = 0.2;
        torJump = torMaxJump;
        torHurt = 0;

        initSpike();
        initCoins();
        initBats(difficulty);
        generateCoffeeDrops();

        //objects
        resetSkins();
        tor.y = 100;
        tor.x = 32;

        ghoul.grab();
        ghoul.x = 220;
        ghoul.y = Math.random(40, 110);

        door.closed();
        door.x = 220;
        door.y = 0;

        platform.x = 220;
        platform.y = Math.random(38, 100);

        intro = true;

        t = 0.0f;
        shake = 0.0f;
        doorShake = 0.0f;

        vamp.x = 300;
        vamp.fly();
        zapTime = 200;
        arrowCoins();
    }

    void initBats(int diff) {
        if (diff > 30) diff = 30;
        bats = new Bat[diff];
        batDead = new boolean[diff];
        int baty = Math.random(0, 100);
        for (int i = 0; i < diff; i++) {
            bats[i] = new Bat();
            bats[i].x = 300 + i * 16;
            bats[i].y = baty + Math.random(-16, 16);
            bats[i].fly();
            batDead[i] = false;
        }
    }

    void initSpike() {
        spike = new Spikes();
        spike.x = Math.random(220, 400);
        spike.y = Math.random(-5, 6) > 0 ? 0 : 124;
        spike.idle();
    }

    void initCoins() {
        coins = new Coin[20];
        int coiny = Math.random(18, 110);
        int startx = Math.random(400, 500);
        for (int i = 0; i < 20; i++) {
            coins[i] = new Coin();
            coins[i].coin();
            coins[i].x = startx + i * 8;
            coins[i].y = coiny + Math.random(-4, 4);
        }
    }

    // Might help in certain situations
    void shutdown() {
        screen = null;
    }

    void resetGame() {
        torCoins += (kills * 2);
        playerRun();
        tor.y = 100;
        torMaxJump = 1;
        torJump = torMaxJump;
        torHits = 3;
        dashCharge = 50;
        dashTime = dashCharge;
        powerReady = 1;
        arrowCoins();
        tailor = false;
        dead = false;
        newHighScore = false;
        t = 0.0f;
        screen.textRightLimit = 220;
        screen.textLeftLimit = 0;
        resetSkins();
        stateManager = 1;
    }

    // update is called by femto.Game every frame
    void update() {
        screen.clear(0);
        t += 2.0f;

        switch (stateManager) {
            case 0: //title
                screen.clear(2);
                updateTitle();
                break;
            case 1: //pre-run start
                updateShop();
                break;
            case 2: //running
                updateScreenShake();

                //Tor dies here.
                if (torHits <= 0) {
                    screen.cameraX = 0;
                    screen.cameraY = 0;
                    dieSound.play();
                    if ((distance / 10) > cookieManager.distScore) {
                        newHighScore = true;
                        cookieManager.distScore = (distance / 10);
                    }
                    if (kills > cookieManager.killScore) cookieManager.killScore = kills;
                    cookieManager.saveCookie();
                    stateManager = 3; //Game Over!
                    tipText = (String) tips[Math.random(0, 8)];
                    return;
                }

                distance += 1 + speed;
                if (distance > nextDifficulty) {
                    difficulty++;
                    nextDifficulty += 1000;
                    door.x = 220;
                    door.closed();
                    doorshut = true;
                }

                //Dungeon background
                updateDrawDungeon();
                //END Dungeon background

                //SPIKES
                updateDrawSpikes();
                //END SPIKES

                //GHOUL
                updateGhoul();
                //END GHOUL

                //BAT
                updateBats();
                //END BAT

                //update VAMPYRE
                updateVampire();
                //END VAMPYRE

                //START PLATFORM
                platform.x -= 1 + speed;
                if (platform.x < -200) {
                    platform.x = Math.random(220, 500);
                    platform.y = Math.random(38, 100);
                }
                platform.draw(screen);
                //END PLATFORM

                //START Draw BG
                screen.fillRect(0, 138, 230, 50, 0);
                //END Draw BG
                if (dashing) speed = 3;
                else speed = 1;

                updateTor();

                checkGhoulGrabTor();

                drawPowerBox();
                drawTorHits();
                drawJumps();

                checkCoffeeCollect();
                updateCoffeeDrops();
                drawCoffeeDrops();

                updateCoins();

                drawUpgrades();

                //DOOR
                updateDrawDoor();
                //DOOR

                drawGameInfo();
                break;
            case 3: //dead
                updateGameOverScreen();
                break;
        }
        screen.flush();
    }
    //END UPDATE

    //TITLE UPDATE
    void updateTitle() {
        screen.fillRect(0, 0, 220, 40, 0);
        screen.fillRect(0, 128, 230, 50, 0);
        drawGround();
        bats[0].setMirrored(true);
        bats[0].draw(screen, 70, 92);
        if (t > 500.0) bats[0].draw(screen, 52, 120);
        if (t > 1000.0) bats[0].draw(screen, 68, 112);
        if (t > 1500.0) bats[0].draw(screen, 50, 106);
        if (t > 2000.0) bats[0].draw(screen, 40, 92);
        if (t > 2500.0) bats[0].draw(screen, 30, 110);
        if (t > 3000.0) bats[0].draw(screen, 22, 98);

        if (t > 3500.0) ghoul.draw(screen, 180, 28);
        if (t > 4000.0) {
            vamp.zap();
            vamp.draw(screen, 170, 100);
        }

        tor.draw(screen, 86, 100);

        titleImage.draw(screen, (screen.width() - titleImage.width()) / 2, 44);
        screen.setTextColor(6);

        screen.setTextPosition(0, 0);
        screen.println("Best run: " + cookieManager.distScore);

        screen.setTextPosition((screen.width() - screen.textWidth("Game Start")) / 2, 150);
        screen.println("Game Start");

        screen.setTextPosition((screen.width() - screen.textWidth("Reset data")) / 2, 160);
        screen.println("Reset data");

        screen.setTextColor(10);
        if (resetData) {
            screen.setTextPosition(60, 160);
            screen.println("->");
        } else {
            screen.setTextPosition(60, 150);
            screen.println("->");
        }

        //Toggle cursor
        if (Button.Down.justPressed() || Button.Up.justPressed()) resetData = !resetData;

        if (Button.C.justPressed() || Button.A.justPressed()) {
            if (resetData) {
                cookieManager.distScore = 0;
                cookieManager.coins = 0;
                cookieManager.killScore = 0;
                cookieManager.saveCookie();

                cookieManager.hasFishBowl = false;
                cookieManager.hasWizHat = false;
                cookieManager.hasHero = false;
                cookieManager.hasTin = false;
                cookieManager.hasRobo = false;
                cookieManager.hasFire = false;
                cookieManager.saveCookie();
            } else {
                stateManager = 1; //Go to preStart 
            }
        }
    }
    //END TITLE UPDATE

    //SHOP UPDATE
    void updateShop() {
        screen.textLeftLimit = 0;

        screen.setTextColor(10);

        //B button switches shop screens.
        if (Button.B.justPressed()) {
            //tailor = !tailor;
            if (!tailor) {
                tailor = true;
                transIn = true;
                tor.x = -16;
                message = "Welcome to the tailor's shop!\nHave a look around with [<-] and [->].";
            } else {
                transOut = true;
                if (!vanityCheck()) {
                    vanity = 0;
                }
            }
        }
        if (tailor) {
            if(transIn){
                if(tor.x < 86) tor.x+=1;
                drawPlayer();
                if(tor.x == 86) transIn = false;
                return;
            }
            if(transOut){
                if(tor.x > -16) tor.x-=1;
                playerMirror(true);
                drawPlayer();
                if(tor.x == -16) {
                    transOut = false;
                    tailor = false;
                    playerMirror(false);
                }
                return;
            }
            if (Button.Right.justPressed()) {
                vanity++;
                swap.play();
                if (vanity > 6) vanity = 6;
                message = vanityMessage();
            }
            if (Button.Left.justPressed()) {
                vanity--;
                swap.play();
                if (vanity < 0) vanity = 0;
                message = vanityMessage();
            }

            if (Button.A.justPressed()) {
                switch (vanity) {
                    case 1:
                        if (!cookieManager.hasWizHat) {
                            if (torCoins >= 500) {
                                torCoins -= 500;
                                cookieManager.hasWizHat = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short " + (500 - torCoins) + " coins.";
                            }
                        }
                        break;
                    case 2:
                        if (!cookieManager.hasFishBowl) {
                            if (torCoins >= 500) {
                                torCoins -= 500;
                                cookieManager.hasFishBowl = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short " + (500 - torCoins) + " coins.";
                            }
                        }
                        break;
                    case 3:
                        if (!cookieManager.hasHero) {
                            if (torCoins >= 750) {
                                torCoins -= 750;
                                cookieManager.hasHero = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short " + (750 - torCoins) + " coins.";
                            }
                        }
                        break;
                    case 4:
                        if (!cookieManager.hasTin) {
                            if (torCoins >= 750) {
                                torCoins -= 750;
                                cookieManager.hasTin = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short" + (750 - torCoins) + " coins.";
                            }
                        }
                        break;
                    case 5:
                        if (!cookieManager.hasRobo) {
                            if (torCoins >= 1000) {
                                torCoins -= 1000;
                                cookieManager.hasRobo = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short" + (1000 - torCoins) + " coins.";
                            }
                        }
                        break;
                    case 6:
                        if (!cookieManager.hasFire) {
                            if (torCoins >= 1000) {
                                torCoins -= 1000;
                                cookieManager.hasFire = true;
                                cookieManager.saveCookie();
                                coinCollect.play();
                            } else {
                                message = message + "\nYou're short" + (1000 - torCoins) + " coins.";
                            }
                        }
                    default:
                        break;
                }
            }
            coinIcon.draw(screen, 0, 8);
            screen.setTextPosition(10, 10);
            screen.println("" + torCoins);
            screen.textLeftLimit = 4;
            screen.drawRect(0, 28, 219, 40, 5);
            screen.fillRect(1, 29, 218, 39, 12);
            if (vanity > 0) {
                coinIcon.draw(screen, 4, 30);
                screen.setTextPosition(14, 32);
            } else screen.setTextPosition(4, 32);
            
            screen.fillRect(0, 130, 220, 6, 4);
            for(int i = 0; i < 10; i++){
                wood.draw(screen, 22 * i, 136);
                wood.draw(screen, 22 * i, 148);
            }
            treadmill.draw(screen, 80, 102);
        } else {
            drawGround();
            screen.fillRect(0, 0, 220, 40, 0);
            screen.fillRect(0, 138, 230, 50, 0);
            drawTrees();
            
            screen.setTextPosition(62, 0);
            screen.println("Press [C] to start.");

            drawUpgrades();
            drawPowerBox();
            drawTorHits();
            drawJumps();

            if (Button.Right.justPressed()) {
                if (powerReady == 2) powerReady = 0;
                else powerReady++;
            }
            if (Button.Left.justPressed()) {
                if (powerReady == 0) powerReady = 2;
                else powerReady--;
            }

            if (Button.A.justPressed()) {
                if(torCoins >= 10){
                    switch (powerReady) {
                        case 0:
                            if (dashCharge < 200) {
                                dashCharge += 10;
                                dashTime = dashCharge;
                                torCoins -= 10;
                                coinCollect.play();
                            }
                            break;
                        case 1:
                            if (torMaxJump < 20) {
                                torMaxJump++;
                                torJump = torMaxJump;
                                torCoins -= 10;
                                coinCollect.play();
                            }
                            break;
                        case 2:
                            if (torHits < 20) {
                                torHits++;
                                torCoins -= 10;
                                coinCollect.play();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            if (Button.C.justPressed() && vanityCheck()) {
                subInit();
                stateManager = 2; //START GAME
            }

            
            //draw text box
            screen.textLeftLimit = 4;
            screen.drawRect(0, 18, 219, 40, 5);
            screen.fillRect(1, 19, 218, 39, 12);
            
            coinIcon.draw(screen, 2, 20);
            screen.setTextPosition(12, 22);
            screen.println("" + torCoins);
            screen.println("Best run: " + cookieManager.distScore);
            message = "[B] Go to tailor.\n[<-] or [->] Select power up.\n[A] to purchase for 10 coins each.";
            for (Coin c: coins) {
                c.draw(screen);
            }
        }
        
        switch (vanity) {
            case 0:
                tor.draw(screen, 86, 100);
                break;
            case 1:
                wizHat.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasWizHat) screen.println("500 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
            case 2:
                fishBowl.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasFishBowl) screen.println("500 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
            case 3:
                hero.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasHero) screen.println("750 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
            case 4:
                tintitto.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasTin) screen.println("750 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
            case 5:
                robot.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasRobo) screen.println("1,000 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
            case 6:
                fireman.draw(screen, 86, 100);
                if (tailor) {
                    if (!cookieManager.hasFire) screen.println("1,000 : [A] Purchase?");
                    else screen.println("[B] to Wear");
                }
                break;
        }
        if (!vanityCheck()) lock.draw(screen, 102, 116);
        screen.println(message);
    }
    //END SHOP UPDATE

    //GAME OVER UPDATE
    void updateGameOverScreen() {
        screen.setTextColor(7);
        if (newHighScore) {
            screen.setTextPosition((screen.width() - screen.textWidth("** New Best Run!! **")) / 2, 0);
            screen.println("** New Best Run!! **");
        } else {
            screen.setTextPosition((screen.width() - screen.textWidth("The dungeon consumed you")) / 2, 0);
            screen.println("The dungeon consumed you");
        }

        //draw message box
        screen.drawRect(10, 32, 200, 90, 5);
        screen.fillRect(11, 33, 199, 89, 12);

        screen.drawRect(10, 130, 200, 30, 5);
        screen.fillRect(11, 131, 199, 29, 12);
        //end box

        screen.setTextColor(6);
        screen.setTextPosition(60, 42);
        screen.textLeftLimit = 60;

        screen.println("Best run: " + cookieManager.distScore);
        screen.println("This run: " + distance / 10);
        screen.println("Coins: " + torCoins);
        screen.println("Kills: " + kills);
        screen.println("Bonus coins: " + (kills * 2));
        screen.drawLine(60, 80, 150, 80, 7, false);
        screen.println("");
        screen.println("Total coins: " + (torCoins + (kills * 2)));

        screen.textLeftLimit = 16;
        screen.textRightLimit = 190;
        screen.setTextPosition(16, 134);
        screen.println(tipText);
        if (Button.C.justPressed()) resetGame();
    }
    //END GAME OVER UPDATE

    boolean vanityCheck() {
        switch (vanity) {
            case 0:
                return true;
            case 1:
                return cookieManager.hasWizHat;
            case 2:
                return cookieManager.hasFishBowl;
            case 3:
                return cookieManager.hasHero;
            case 4:
                return cookieManager.hasTin;
            case 5:
                return cookieManager.hasRobo;
            case 6:
                return cookieManager.hasFire;
            default:
                return false;
        }
    }

    String vanityMessage() {
        switch (vanity) {
            case 1:
                return "The Wizard Robes! With complementary\nKitty! No idea where he came from.";
            case 2:
                return "Fish Bowl. If you like to swim?";
            case 3:
                return "This one links some memories.\nThe Hero suit!";
            case 4:
                return "Adventure awaits! Nice hair cut.";
            case 5:
                return "Time to Rock and Roll!";
            case 6:
                return "This will keep you warm in any\ndungeon!";
            default:
                return "[B] to return to Power Up shop.";
        }
    }

    void updateDrawDungeon() {
        dgnX -= 1 + speed;
        if (dgnX <= -220) {
            dgnX = 0;
            intro = false;
        }
        if (dgnX > -220 && intro) {
            introGround();
            frontBackground.draw(screen, dgnX + 96, 140 - frontBackground.height());
            for (int i = 0; i < 4; i++) {
                dungeonBackground.draw(screen, dgnX + 220, i * 44);
            }
            screen.fillRect(dgnX + 220, 128, 400, 10, 2);
        } else {
            for (int i = 0; i < 4; i++) {
                dungeonBackground.draw(screen, dgnX, i * 44);
                dungeonBackground.draw(screen, dgnX + 220, i * 44);
            }
            screen.fillRect(0, 128, 220, 10, 2);
            drawCarpet();
        }
    }

    void updateDrawSpikes() {
        if (difficulty >= 10) {
            spike.x -= 1 + speed;

            if (spike.x < -140) {
                initSpike();
            }
            if (spike.y == 0) {
                spike.setFlipped(true);
            } else {
                spike.setFlipped(false);
            }

            spike.draw(screen);
            if (collidesWithTor(spike.x, spike.y, spike.width(), spike.height())) {
                torHurt = cooldown;
                torHits = 0;
                //SO DEAD. GameOver if touch any spikes.
            }
        }
    }

    void updateScreenShake() {
        if (shake > 0.0) {
            if (dashing) {
                screen.cameraX = Math.cos(t) * 1;
            } else {
                screen.cameraX = Math.cos(t) * 3;
                screen.cameraY = Math.sin(t) * 3;
            }
            shake -= 0.2f;
        }

        if (doorShake > 0.0) {
            screen.cameraX = Math.cos(t) * 10;
            screen.cameraY = Math.sin(t) * 10;
            doorShake -= 0.2f;
        }

        if (doorShake <= 0.0 && shake <= 0.0) {
            screen.cameraX = 0;
            screen.cameraY = 0;
        }
    }

    void updateDrawDoor() {
        if (door.x > -10) {
            door.x -= 1 + speed;
            
            if (tor.y > 16 && tor.y + tor.height() < 114 && tor.x + tor.width() / 2 > door.x && doorshut) {
                if (dashing) {
                    door.broken();
                    doorShake = 3.0f;
                    explode.play();
                    doorshut = false;
                } else {
                    torHurt = cooldown;
                    torHits = 0;
                    //SO DEAD. GameOver
                }
            }
            if ((tor.y < 16 || tor.y + tor.height() > 114) && (tor.x + tor.width() / 2) > door.x && doorshut) {
                torHurt = cooldown;
                torHits = 0;
                //SO DEAD. GameOver
            }
            door.draw(screen);
        }
    }

    void drawGameInfo() {
        //Draw position
        screen.setTextPosition(0, 0);
        screen.setTextColor(10);
        screen.println("Distance: " + distance / 10);
        screen.println("Kills: " + kills);
        screen.println("Coins: " + torCoins);
    }

    boolean collidesWithTor(float x, float y, float width, float height) {
        return tor.x + 4 < x + width && tor.x + 4 + tor.width() - 8 > x &&
            tor.y + 4 < y + height && tor.y + 4 + tor.height() - 8 > y;
    }

    void updateVampire() {
        if (difficulty >= 25) {
            if (vamp.x <= -200) {
                if (Math.random(0, 20) == 5) {
                    vamp.x = 600;
                    vamp.y = tor.y;
                    vampDead = false;
                }
            }

            if (!vampDead) {
                if (!(vamp.x <= tor.x + tor.width())) {
                    vamp.x -= 0.1;
                }
                if (vamp.y > tor.y) vamp.y -= 0.3f;
                else vamp.y += 0.3f;

                if (vamp.x <= 110) {
                    vamp.transform();
                }
                if (vamp.x <= 80) {
                    vamp.idle();
                }

                if (vamp.y >= tor.y - 10 && vamp.y + vamp.height() < tor.y + tor.height() + 10 && vamp.x <= tor.x + tor.width() + 10) {
                    vamp.zap();
                    zapTime--;
                }

                if (zapTime <= 0) {
                    screen.drawCircle(tor.x + tor.width() / 2, tor.y + tor.height() / 2, 4, 5, false);
                    zapTime = 200;
                    torHits--;
                    torHurt = cooldown;
                }

                if (zapTime < 100) {
                    if (dashing && tor.y >= vamp.y && tor.y <= vamp.y + vamp.height()) {
                        vamp.x -= 1 + speed;
                        if (vamp.x < tor.x + tor.width()) {
                            vampDead = true;
                            vamp.x = -200;
                        }
                    }
                }

                vamp.draw(screen);
            }
        }
    }

    void updateCoins() {
        int check = 0;
        for (int i = 0; i < coins.length; i++) {
            coins[i].x -= 1 + speed;
            coins[i].y += Math.random(-1, 2);
            coins[i].draw(screen);
            if (coins[i].x < -100) check++;
            if (collidesWithTor(coins[i].x, coins[i].y, coins[i].width(), coins[i].height())) {
                coins[i].x = -200;
                torCoins++;
                coinCollect.play();
            }
        }
        if (check == coins.length) initCoins();
    }

    void updateBats() {
        int check = 0;
        for (int i = 0; i < bats.length; i++) {
            if (bats[i].x < -10) check++;
            if (!batDead[i]) {
                bats[i].x -= 2 + speed;
            } else {
                bats[i].x -= 1 + speed;
            }
            if (collidesWithTor(bats[i].x, bats[i].y, bats[i].width(), bats[i].height())) {
                if (dashing && !batDead[i]) {
                    bats[i].x += Math.random(16, 23);
                    bats[i].y += Math.random(-10, 10);
                    bats[i].dead();
                    batDead[i] = true;
                    kills++;
                    batSplat.play();
                } else if (torHurt <= 0 && !batDead[i]) {
                    torHurt = cooldown;
                    torHits -= 1;
                    hurt.play();
                    shake = 2.0;
                }
            }

            bats[i].draw(screen);
        }

        if (check >= bats.length) initBats(difficulty);
    }

    void updateGhoul() {
        ghoul.x -= 1 + speed;
        if (ghoul.x <= -20) {
            ghoul.x = Math.random(220, 600);
            ghoul.y = Math.random(50, 100);
            if (platform.x < ghoul.x + ghoul.width() &&
                platform.x + platform.width() > ghoul.x &&
                platform.y < ghoul.y + ghoul.height() &&
                platform.y + platform.height() > ghoul.y) {
                ghoul.x += 160;
            }
            ghoul.grab();
            ghoulDead = false;
        }
        ghoul.draw(screen);
    }

    void checkGhoulGrabTor() {
        // screen.drawRect(ghoul.x+8, ghoul.y+2, ghoul.width()-12, ghoul.height()-16, 10, false);
        if (collidesWithTor(ghoul.x+8, ghoul.y+2, ghoul.width()-12, ghoul.height() - 16)) {
            if (dashing && !ghoulDead) {
                ghoul.dead();
                ghoulDead = true;
                kills++;
                splat.play();
            } else {
                if (torHurt <= 0 && !ghoulDead) {
                    torHits -= 1;
                    hurt.play();
                    shake = 2.0;
                    torHurt = cooldown;
                }
            }
        }
    }

    void checkCoffeeCollect() {
        if (collidesWithTor(coffee.x, coffee.y, coffee.width(), coffee.height())) {
            powerReady += 1;
            if (powerReady >= 3) powerReady = 0;
            coffee.x = -20;
        }
    }

    boolean checkTorPlatform() {
        if (dys < 0.0) return false; //jumping
        if (tor.x < platform.x + platform.width() &&
            tor.x + tor.width() > platform.x &&
            tor.y + tor.height() - 6 + dys < platform.y + 10 &&
            tor.y + tor.height() - 6 + dys > platform.y + 3) {
            if (!Button.Down.isPressed()) {
                dys = 0;
                torJump = torMaxJump;
                if (!dashing) {
                    playerRun();
                }
                return true;
            }

        }
        return false;
    }

    void updateTor() {
        tor.x = 32;
        if (jump && !dashing) {
            dys += torGravity; //Gravity
            if (Button.Down.isPressed()) {
                dys += 0.4;
            }
        } else {
            dys = 0;
        }

        if (tor.y <= 0) {
            tor.y = 0;
        }

        if (!checkTorPlatform()) {
            if (tor.y >= 100) {
                tor.y = 100;
                jump = false;
                torJump = torMaxJump;
                playerRun();
            }
        }

        //INPUT
        if (Button.A.justPressed() && (!jump || torJump > 0)) {
            dys = -3.5;
            torJump--;
            jump = true;
            jumpSound.play();
            playerJump();
        }

        if (Button.B.isPressed() && dashTime > 0 && dashReady && torHurt <= 0) {
            dashTime--;
            dashing = true;
            shake += 0.2f;
            playerDash();
        }

        if (Button.C.justPressed()) {
            switch (powerReady) {
                case 0:
                    powerReady = -1;
                    if (dashCharge < 200) dashCharge += 10;
                    dashTime = dashCharge;
                    powerUp.play();
                    break;
                case 1:
                    if (torMaxJump < 20) torMaxJump++;
                    torJump = torMaxJump;
                    powerReady = -1;
                    powerUp.play();
                    break;
                case 2:
                    if (torHits < 20) torHits++;
                    powerReady = -1;
                    powerUp.play();
                    break;
                default:
                    break;
            }
        }

        if (!Button.B.isPressed() && dashing && torHurt <= 0) {
            dashing = false;
            playerJump();
        }
        //END Input

        if (!dashReady) {
            dashTime++;
            if (dashTime >= dashCharge) {
                dashTime = dashCharge;
                dashReady = true;
            }
        }

        if (dashTime <= 0 && torHurt <= 0) {
            dashing = false;
            dashReady = false;
            playerJump();
        }

        if (torHurt >= 0) {
            torHurt--;
        }

        tor.y += dys;
        drawPlayer();
    }

    void generateCoffeeDrops() {
        coffee.x = Math.random(220, 230);
        coffee.y = Math.random(30, 120);
    }

    void updateCoffeeDrops() {
        coffee.x -= 1 + speed;
        if (coffee.x < -10) {
            generateCoffeeDrops();
        }
    }
    void drawCoffeeDrops() {
        coffee.y += Math.random(-1, 2);
        coffee.draw(screen);
    }

    void arrowCoins() {
        coins = new Coin[12];
        for (int i = 0; i < 12; i++) {
            coins[i] = new Coin();
            coins[i].coin();
        }

        coins[0].x = 160;
        coins[0].y = 75;

        coins[1].x = 170;
        coins[1].y = 75;

        coins[2].x = 180;
        coins[2].y = 75;

        coins[9].x = 190;
        coins[9].y = 59;
        coins[6].x = 190;
        coins[6].y = 67;
        coins[3].x = 190;
        coins[3].y = 75;
        coins[7].x = 190;
        coins[7].y = 83;
        coins[8].x = 190;
        coins[8].y = 91;

        coins[10].x = 200;
        coins[10].y = 67;
        coins[11].x = 200;
        coins[11].y = 83;
        coins[4].x = 200;
        coins[4].y = 75;

        coins[5].x = 210;
        coins[5].y = 75;

    }

    void drawUpgrades() {
        screen.setTextColor(10);
        screen.setTextPosition(10, 142);
        screen.print("POWER");

        screen.setTextPosition(54, 142);
        screen.print("JUMP");

        screen.setTextPosition(98, 142);
        screen.print("HEALTH");

        for (int i = 0; i < 3; i++) {
            if (i == powerReady) {
                screen.drawRect(44 * i + 8, 140, 40, 8, 5);
            } else {
                screen.drawRect(44 * i + 8, 140, 40, 8, 10);
            }
        }
    }

    void drawPowerBox() {
        //draw charge box
        screen.drawRect(8, 152, dashCharge + 1, 4, 5);
        screen.fillRect(9, 153, dashTime, 3, 6);
    }

    void drawTorHits() {
        for (int j = 0; j < torHits; j++) {
            screen.drawCircle(10 * j + 10, 170, 2, 5);
        }
    }

    void drawJumps() {
        for (int i = 0; i < torJump; i++) {
            screen.drawCircle(10 * i + 10, 162, 2, 10);
        }
    }

    void introGround() {
        rockwayX -= 1 + speed;
        if (rockwayX <= -220) rockwayX = 0;
        for (int i = 0; i < 5; i++) {
            rockway.draw(screen, 22 * i + rockwayX, 128);
        }
    }

    void drawGround() {
        rockwayX -= 1;
        if (rockwayX <= -220) rockwayX = 0;
        for (int i = 0; i < 20; i++) {
            rockway.draw(screen, 22 * i + rockwayX, 128);
        }
    }

    void drawCarpet() {
        cpax -= 1 + speed;
        cpbx -= 1 + speed;
        if (cpax <= -100) cpax = 220 + Math.random(0, 200);
        if (cpbx <= -100) cpbx = 220 + Math.random(0, 200);

        carpetA.draw(screen, cpax, 128);
        carpetB.draw(screen, cpbx, 128);
    }

    void drawTrees() {
        tbx -= 1;
        tcx -= 1;
        treeA.x -= 1;
        if (treeA.x <= -60) {
            treeA.x = Math.random(220, 400);
            if (Math.random(0, 2) == 1) treeA.setMirrored(true);
            else treeA.setMirrored(false);
        }
        if (tbx <= -60) tbx = Math.random(220, 400);
        if (tcx <= -60) tcx = Math.random(220, 400);

        treeA.draw(screen);
        treeB.draw(screen, tbx, 129 - treeB.height());
        treeC.draw(screen, tcx, 129 - treeC.height());
    }

    void resetSkins() {
        tor.run();
        wizHat.run();
        fishBowl.run();
        hero.run();
        tintitto.run();
        robot.run();
        fireman.run();
    }

    void drawPlayer() {
        if (vanity == 3 && torHurt > 0) {
            screen.setTextPosition(tor.x - 16, tor.y - 8);
            screen.setTextColor(10);
            screen.println("HEY! LISTEN!");
        }
        if (torHurt > 0 && torHurt % 3 == 1) {
            return;
        }
        switch (vanity) {
            case 0:
                tor.draw(screen);
                break;
            case 1:
                wizHat.draw(screen, tor.x, tor.y);
                break;
            case 2:
                fishBowl.draw(screen, tor.x, tor.y);
                break;
            case 3:
                hero.draw(screen, tor.x, tor.y);
                break;
            case 4:
                tintitto.draw(screen, tor.x, tor.y);
                break;
            case 5:
                robot.draw(screen, tor.x, tor.y);
                break;
            case 6:
                fireman.draw(screen, tor.x, tor.y);
                break;
        }
    }

    void playerRun() {
        switch (vanity) {
            case 0:
                tor.run();
                break;
            case 1:
                wizHat.run();
                break;
            case 2:
                fishBowl.run();
                break;
            case 3:
                hero.run();
                break;
            case 4:
                tintitto.run();
                break;
            case 5:
                robot.run();
                break;
            case 6:
                fireman.run();
                break;
        }
    }

    void playerDash() {
        switch (vanity) {
            case 0:
                tor.dash();
                break;
            case 1:
                wizHat.dash();
                break;
            case 2:
                fishBowl.dash();
                break;
            case 3:
                hero.dash();
                break;
            case 4:
                tintitto.dash();
                break;
            case 5:
                robot.dash();
                break;
            case 6:
                fireman.dash();
                break;
        }
    }

    void playerJump() {
        switch (vanity) {
            case 0:
                tor.jump();
                break;
            case 1:
                wizHat.jump();
                break;
            case 2:
                fishBowl.jump();
                break;
            case 3:
                hero.jump();
                break;
            case 4:
                tintitto.jump();
                break;
            case 5:
                robot.jump();
                break;
            case 6:
                fireman.jump();
                break;
        }
    }
    
    void playerMirror(boolean value){
        switch (vanity) {
            case 0:
                tor.setMirrored(value);
                break;
            case 1:
                wizHat.setMirrored(value);
                break;
            case 2:
                fishBowl.setMirrored(value);
                break;
            case 3:
                hero.setMirrored(value);
                break;
            case 4:
                tintitto.setMirrored(value);
                break;
            case 5:
                robot.setMirrored(value);
                break;
            case 6:
                fireman.setMirrored(value);
                break;
        }
    }
}