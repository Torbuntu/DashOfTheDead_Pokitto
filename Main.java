import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import JavaDashPalette;
import femto.font.TIC80;

import Tor;
import Coin;
import Coffee;
import enemies.Ghoul;
import enemies.Bat;
import enemies.Spikes;

import backgrounds.Door;

import backgrounds.DungeonBackground;
import backgrounds.FrontBackground;
import StartBackground;

import backgrounds.Platform;

class HighScore extends femto.Cookie {
    HighScore(){
        super();
        begin("DDScore");
    }
    int score;
}


class Main extends State {

    static final var save = new HighScore();

    HiRes16Color screen; // the screenmode we want to draw with

    boolean jump = false, dashing = false, dashReady = true, ghoulDead = false, intro = true, preStart = true, coinIntro = true;
    Tor tor;
    Coffee coffee;
    Ghoul ghoul;
    Bat[] bats;
    Spikes spike;
    boolean[] batDead;
    Coin[] coins;
    Door door;
    DungeonBackground dungeonBackground;
    FrontBackground frontBackground;
    Platform platform;
    
    float dxs, dys, torGravity;
    int dashTime, speed, powerReady = 1, torJump, dgnX, torHurt, distance, kills;
    float t, shake, doorShake;
    int difficulty, nextDifficulty, cooldown, dashCharge = 50, torMaxJump = 1, torHits = 3 ;
    
    static int torCoins;
    //prestart screen variables
    int cursor, shopCharge = 50, shopJump = 1, shopHits = 3;
    boolean tailor = false;
    int sbx = 0;
    StartBackground startBackground;
    
    //title screen variables
    boolean title = true;

    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    void init(){
        screen = new HiRes16Color(JavaDashPalette.palette(), TIC80.font());
        cursor = 0;
        
        torCoins = 0;

        tor = new Tor();
        
        ghoul = new Ghoul();

        frontBackground = new FrontBackground();
        dungeonBackground = new DungeonBackground();
        startBackground = new StartBackground();
        
        door = new Door();
        
        platform = new Platform();
        platform.idle();

        coffee = new Coffee();
        coffee.idle();
        
        subInit();
    }
    
    void subInit(){
        
        difficulty = 1;
        nextDifficulty = 500;
        distance = 0;
        powerReady = -1;
        kills = 0;
        cooldown = 75;
        dgnX = 0;
        
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
        tor.y = 100;
        tor.x = 32;
        tor.run();
        
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
        
        arrowCoins();
    }
    
    void initBats(int diff){
        if(diff > 30) diff = 30;
        bats = new Bat[diff];
        batDead = new boolean[diff];
        int baty = Math.random(0, 100);
        for(int i = 0; i < diff; i++){
            bats[i] = new Bat();
            bats[i].x = 300 + i * 16;
            bats[i].y = baty + Math.random(-16, 16);
            bats[i].fly();
            batDead[i] = false;
        }
    }
    
    void initSpike(){
        spike = new Spikes();
        spike.x = Math.random(220, 400);
        spike.y = Math.random(-5, 6) > 0 ? 0 : 124;
        spike.idle();
    }
    
    void initCoins(){
        coins = new Coin[20];
        int coiny = Math.random(18, 110);
        int startx = Math.random(400, 500);
        for(int i = 0; i < 20; i++){
            coins[i] = new Coin();
            coins[i].coin();
            coins[i].x = startx + i*8; 
            coins[i].y = coiny + Math.random(-4, 4);
        }
    }
    
    void arrowCoins(){
        coins = new Coin[12];
        for(int i = 0; i < 12; i++){
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
    
    void generateCoffeeDrops(){
        coffee.x = Math.random(220, 230);
        coffee.y = Math.random(30, 120);
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    void updateShop(){
        sbx -= 1;
        if(sbx <= -220) sbx = 0;
        startBackground.draw(screen, sbx, 0);
        startBackground.draw(screen, sbx+220, 0);
        screen.setTextPosition(0, 0);
        screen.setTextColor(6);
        screen.println("Press [C] to start.");
        screen.println("Coins: " + torCoins);

        drawUpgrades();
        drawPowerBox();
        drawTorHits();
        drawJumps();
        
        if(Button.Right.justPressed()){
            if(powerReady == 2) powerReady = 0;
            else powerReady++;
        }
        if(Button.Left.justPressed()){
            if(powerReady == 0) powerReady = 2;
            else powerReady--;
        }
        
        if(Button.A.justPressed()){
            switch(powerReady){
                case 0:
                    if(dashCharge < 200) dashCharge += 10;
                    dashTime = dashCharge;
                    break;
                case 1:
                    if(torMaxJump < 20) torMaxJump++;
                    torJump = torMaxJump;
                    break;
                case 2:
                    if(torHits < 20) torHits++;
                    break;
                default:
                    break;
            }
        }
    
        if(Button.C.justPressed()) {
            preStart = false;
            subInit();
        }
        
        tor.draw(screen);
    }
    
    void updateTitle(){
        screen.setTextColor(6);
        screen.setTextPosition(0,0);
        screen.println("Press [C] to play");
        screen.println("High score: " + save.score);
        if(Button.C.justPressed()){
            preStart = true;
            title = false;
        }
        screen.flush();
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
        t += 2.0f;

        if(title){
            updateTitle();
            return;
        }
        
        if(preStart){
            updateShop();
            for(Coin c : coins){
                c.draw(screen);
            }
            screen.flush();
            return;
        }
        
        if(shake > 0.0){
            if(dashing){
                screen.cameraX = Math.cos(t) * 1;
            }else{
                screen.cameraX = Math.cos(t) * 3;
                screen.cameraY = Math.sin(t) * 3;
            }
            shake -= 0.2f;
        }
        
        if(doorShake > 0.0){
            screen.cameraX = Math.cos(t) * 10;
            screen.cameraY = Math.sin(t) * 10;
            doorShake -= 0.2f;
        }
        
        if(doorShake <= 0.0 && shake <= 0.0){
            screen.cameraX = 0;
            screen.cameraY = 0;
        }
        
        if(torHits <= 0) {
            if(distance > save.score){
                save.score = distance;
                save.saveCookie();
            }   
            preStart = true;
            torMaxJump = 1;
            torJump = torMaxJump;
            torHits = 3;
            dashCharge = 50;
            dashTime = dashCharge;
            powerReady = 1;
            
        }
        
        distance+=1+speed;
        if(distance > nextDifficulty) {
            difficulty++;
            nextDifficulty += 1000;
            door.x = 220;
            door.closed();
        }
        //Dungeon background
        dgnX -= 1+speed;
        if(dgnX <= -220) {
            dgnX = 0;
            intro = false;   
        }
        if(dgnX > -220 && intro){
            frontBackground.draw(screen, dgnX, 0);
            dungeonBackground.draw(screen, dgnX+220, 0);
        }else{
            dungeonBackground.draw(screen, dgnX-220, 0);
            dungeonBackground.draw(screen, dgnX, 0);
            dungeonBackground.draw(screen, dgnX+220, 0);
        }
        //END Dungeon background
        
        //SPIKES
        if(difficulty >= 10){
            spike.x -= 1+speed;
            
            if(spike.x < -140){
                initSpike();
            } 
            if(spike.y == 0){
                spike.setFlipped(true);
            }else{
                spike.setFlipped(false);
            }
            
            spike.draw(screen);
            if(collidesWithTor(spike.x, spike.y, spike.width(), spike.height())){
                torHurt = cooldown;
                torHits = 0;
                //SO DEAD. GameOver if touch any spikes.
            }
        }
        //END SPIKES
        
        //GHOUL
        ghoul.x-=1+speed;
        if(ghoul.x <= -20){
            ghoul.x = Math.random(220, 600);
            ghoul.y = Math.random(50, 100);
            if(platform.x < ghoul.x + ghoul.width() &&
                platform.x + platform.width() > ghoul.x &&
                platform.y < ghoul.y + ghoul.height() &&
                platform.y + platform.height() > ghoul.y){
                ghoul.x += 160;
            }
            ghoul.grab();
            ghoulDead = false;
        }
        ghoul.draw(screen);
        //END GHOUL
        
        //BAT
        updateBats();
        //END BAT
        
        //START PLATFORM
        platform.x -= 1+speed;
        if(platform.x < -200){
            platform.x = Math.random(220, 500);
            platform.y = Math.random(38, 100);
        }
        platform.draw(screen);
        //END PLATFORM
        
        //START Draw BG
        screen.fillRect(0, 138, 230, 50, 0);
        //END Draw BG
        if(dashing)speed = 3;
        else speed = 1;
        
        updateTor();
        if(torHurt >= 0)torHurt--;
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
        if(door.x > -10){
            door.x -= 1 + speed;
            if(tor.y > 20 && tor.y + tor.height() < 108 && tor.x+tor.width() > door.x && dashing){
                door.broken();
                doorShake = 3.0f;
            }
            door.draw(screen);
        }
        //DOOR
        
        //Draw position
        screen.setTextPosition(0, 0);
        screen.setTextColor(10);
        screen.println("Distance: "+distance);
        screen.println("Kills: " + kills);
        screen.println("Coins: " + torCoins);

        screen.flush();
    }
    
    boolean collidesWithTor(float x, float y, float width, float height){
        return tor.x + 4 < x + width && tor.x + 4 + tor.width() - 8 > x 
                && tor.y + 4 < y + height && tor.y + 4 + tor.height() - 8 > y;
    }

    void updateCoins(){
        int check  = 0;
        for(int i = 0; i < coins.length; i++){
            coins[i].x -= 1+speed;
            coins[i].y += Math.random(-1, 2);
            coins[i].draw(screen);
            if(coins[i].x < -100)check++;
            if(collidesWithTor(coins[i].x, coins[i].y, coins[i].width(), coins[i].height())){
                coins[i].x = -200;
                torCoins++;
            }
        }
        if(check == 20) initCoins();
    }

    void updateBats(){
        int check = 0;
        for(int i = 0; i < bats.length; i++){
            if(bats[i].x < -10) check++;
            if(!batDead[i]) {
                bats[i].x -= 2+speed;
            }else{
                bats[i].x -= 1+speed;
            }
            if(collidesWithTor(bats[i].x, bats[i].y, bats[i].width(), bats[i].height())){
                if(dashing && !batDead[i]){
                    bats[i].x += Math.random(16, 23);
                    bats[i].y += Math.random(-10, 10);
                    bats[i].dead();
                    batDead[i] = true;
                    kills++;
                }else if (torHurt <= 0 && !batDead[i]){
                    torHurt = cooldown;
                    torHits-=1;
                    shake = 2.0;
                }
            }
        
            bats[i].draw(screen);
        }
        
        if(check >= bats.length) initBats(difficulty);
    }
    
    void checkGhoulGrabTor(){
        // screen.drawRect(ghoul.x, ghoul.y, ghoul.width(), ghoul.height()-8, 10, false);
        if(collidesWithTor(ghoul.x, ghoul.y, ghoul.width(), ghoul.height()-8)){
            if(dashing && !ghoulDead){
                ghoul.dead();
                ghoulDead = true;
                kills++;
            }else{
                if(torHurt <= 0 && !ghoulDead){
                    torHits-=1;
                    shake = 2.0;
                    torHurt = cooldown;
                }
            }
        }
    }
    
    void checkCoffeeCollect(){
        if(collidesWithTor(coffee.x, coffee.y, coffee.width(), coffee.height())){
            powerReady += 1;
            if(powerReady >= 3) powerReady = 0;
            coffee.x = -20;
        }
    }
    
    boolean checkTorPlatform(){
        if(dys < 0.0)return false;//jumping
        if(tor.x < platform.x + platform.width() &&
            tor.x + tor.width() > platform.x &&
            tor.y + tor.height()-6 + dys< platform.y+10 &&
            tor.y + tor.height()-6 + dys > platform.y+3)
            {
                if(!Button.Down.isPressed() ){
                    dys = 0;
                    torJump = torMaxJump;
                    if(!dashing && torHurt <= 0)tor.run();
                    return true;
                }
               
            }
        return false;
    }
    
    
    void updateTor(){
        if(jump && !dashing){
            dys += torGravity;//Gravity
            if(Button.Down.isPressed()){
                dys += 0.4;
            }
        }else{
            dys = 0;
        }
        
        if(tor.y <= 0){
            tor.y = 0;
        }
        
        if(!checkTorPlatform()){
            if(tor.y >= 100){
                tor.y = 100;
                jump = false;
                torJump = torMaxJump;
                if(!dashing && torHurt <= 0)tor.run();
            }
        }

        //INPUT
        if( Button.A.justPressed() && (!jump || torJump > 0)) {
            dys = -3.5;
            torJump--;
            jump = true;  
            tor.jump();
        }
        
        if(Button.B.isPressed() && dashTime > 0 && dashReady && torHurt <= 0){
            dashTime--;
            dashing = true;
            shake += 0.2f;
            tor.dash();
        }

        if(Button.C.justPressed()){
            switch(powerReady){
                case 0:
                    powerReady = -1;
                    if(dashCharge < 200) dashCharge += 10;
                    dashTime = dashCharge;
                    break;
                case 1:
                    if(torMaxJump < 50) torMaxJump++;
                    torJump = torMaxJump;
                    powerReady = -1;
                    break;
                case 2:
                    if(torHits < 20) torHits++;
                    powerReady = -1;
                    break;
                default:
                    break;
            }
        }
        
        
        
        if(!Button.B.isPressed() && dashing && torHurt <= 0){
            dashing = false;
            tor.jump();
        }
        //END Input
        
        if(!dashReady){
            dashTime++;
            if(dashTime >= dashCharge){
                dashTime = dashCharge;
                dashReady = true;
            }
        }
        
        if(dashTime <= 0 && torHurt <= 0){
            dashing = false;
            dashReady = false;
            tor.jump();
        }
        
        if(torHurt >= 0) tor.hurtRun();
        
        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
    }

    void updateCoffeeDrops(){
        coffee.x-= 1+speed;
        if(coffee.x < -10){
            generateCoffeeDrops();
        }
    }
    void drawCoffeeDrops(){
        coffee.y += Math.random(-1, 2);
        coffee.draw(screen);
    }
    
    void drawUpgrades(){
        screen.setTextColor(10);
        screen.setTextPosition(10, 142);
        screen.print("PWR");
        
        screen.setTextPosition(34, 142);
        screen.print("JMP");
        
        screen.setTextPosition(58, 142);
        screen.print("HIT");
        
        for(int i = 0; i < 3; i++){
            if(i==powerReady){
                screen.drawRect(24*i+8, 140, 20, 8, 5);
            }else{
                screen.drawRect(24*i+8, 140, 20, 8, 10);
            }
        }
    }
    
    void drawPowerBox(){
        //draw charge box
        screen.drawRect(8, 152, dashCharge+1, 4, 5);
        screen.fillRect(9, 153, dashTime, 3, 6);
    }
    
    void drawTorHits(){
        for(int j = 0; j < torHits; j++){
            screen.drawCircle(10*j+10, 170, 2, 5);
        }
    }
    
    void drawJumps(){
        for(int i = 0; i < torJump; i++){
            screen.drawCircle(10*i+10, 162, 2, 10);
        }
    }

}
