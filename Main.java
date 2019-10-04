import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import JavaDashPalette;
import femto.font.TIC80;

import Tor;
import Coin;
import enemies.Ghoul;
import enemies.Bat;
import enemies.Spikes;

import backgrounds.Door;

import backgrounds.DungeonBackground;
import backgrounds.FrontBackground;

import backgrounds.Platform;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    boolean jump = false, dashing = false, dashReady = true, ghoulDead = false, intro = true;
    Tor tor;
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
    int dashTime, dashCharge, speed, torSpeed, powerReady, torHits, torMaxJump, torJump, dgnX, torHurt, torCoins, distance, kills;
    
    int quantity, coffeeY, difficulty, nextDifficulty;
    int[] coffees;

    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(JavaDashPalette.palette(), TIC80.font());

        kills = 0;
        torCoins = 0;

        tor = new Tor();
        tor.y = 100;
        tor.x = 32;
        tor.run();
        
        ghoul = new Ghoul();
        ghoul.grab();
        ghoul.x = 220;
        ghoul.y = Math.random(40, 110);
        
        initSpike();
        initCoins();
        
        frontBackground = new FrontBackground();

        difficulty = 1;
        nextDifficulty = 500;
        initBats(difficulty);
        dungeonBackground = new DungeonBackground();
        dgnX = 0;
        
        door = new Door();
        door.closed();
        door.x = 220;
        door.y = 0;

        platform = new Platform();
        platform.idle();
        platform.x = 220;
        platform.y = Math.random(38, 100);

        dashTime = 0;
        dashCharge = 50;
        speed = 1;
        torSpeed = 0;
        torGravity = 0.2;
        torHits = 3;
        torMaxJump = 1;
        torJump = 1;
        torHurt = 0;
        
        distance = 0;

        quantity = 0;
        powerReady = -1;
        int y = Math.random(70, 120);
        generateCoffeeDrops(y);
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
    
    void generateCoffeeDrops( int y ){
        coffees = new int[6];
        int start = Math.random(220, 230);
        for(int i = 0; i < 6; i++){
            coffees[i] = 8*i+start;
        }
        coffeeY = y;
    }
    
    // Might help in certain situations
    void shutdown(){
        screen = null;
    }
    
    // update is called by femto.Game every frame
    void update(){
        screen.clear(0);
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
            
            if(tor.x < spike.x + spike.width() &&
                tor.x + tor.width() > spike.x &&
                tor.y + 4 < spike.y + spike.height() &&
                tor.y + 4 + tor.height() - 4 > spike.y){
                    torHurt = 150;
                    torHits-=1;
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
                platform.y < ghoul.y + ghoul.height()-8 &&
                platform.y + platform.height() > ghoul.y){
                ghoul.x += 100;
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
        
        updatePower();
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

    void updateCoins(){
        int check  = 0;
        for(int i = 0; i < 20; i++){
            coins[i].x -= 1+speed;
            coins[i].y += Math.random(-1, 2);
            coins[i].draw(screen);
            if(coins[i].x < -100)check++;
            
            if(tor.x < coins[i].x + coins[i].width() &&
                tor.x + tor.width() > coins[i].x &&
                tor.y + 4 < coins[i].y + coins[i].height() &&
                tor.y + 4 + tor.height() - 4 > coins[i].y){
                    coins[i].x = -200;
                    torCoins++;
                }
        }
        if(check == 20) initCoins();
    }

    void updateBats(){
        int check = 0;
        //screen.drawRect(tor.x, tor.y + 4, tor.width(), tor.height() - 4, 5, false);
        for(int i = 0; i < bats.length; i++){
            if(bats[i].x < -10) check++;
            if(!batDead[i]) {
                bats[i].x -= 2+speed;
            }else{
                bats[i].x -= 1+speed;
            }
            //screen.drawRect(bats[i].x, bats[i].y, bats[i].width(), bats[i].height(), 5, false);
            if(tor.x < bats[i].x + bats[i].width() &&
                tor.x + tor.width() > bats[i].x &&
                tor.y + 4 < bats[i].y + bats[i].height() &&
                tor.y + 4 + tor.height() - 4 > bats[i].y){
                if(dashing && !batDead[i]){
                    bats[i].x += Math.random(16, 23);
                    bats[i].y += Math.random(-10, 10);
                    bats[i].dead();
                    batDead[i] = true;
                    kills++;
                }else if (torHurt <= 0 && !batDead[i]){
                    torHurt = 150;
                    torHits-=1;
                }
            }
        
            bats[i].draw(screen);
        }
        
        if(check >= bats.length) initBats(difficulty);
    }
    
    void checkGhoulGrabTor(){
        if(tor.x < ghoul.x + ghoul.width() &&
            tor.x + tor.width() > ghoul.x &&
            tor.y+4 < ghoul.y + ghoul.height()-8 &&
            tor.y+4 + tor.height()-8 > ghoul.y)
            {
                if(dashing && !ghoulDead){
                    ghoul.dead();
                    ghoulDead = true;
                    kills++;
                }else{
                    if(torHurt <= 0 && !ghoulDead){
                        torHits-=1;
                        torHurt = 150;
                    }
                }
            }
    }
    
    void checkCoffeeCollect(){
        for(int x = 0; x < 6; x++){
            if(tor.x < coffees[x] + 4 && tor.x + tor.width() > coffees[x] &&
                tor.y + 4 < coffeeY + 6 && tor.y + 4 + tor.height() - 4 > coffeeY)
            {
                quantity++;
                coffees[x] = -20;
            }
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
            tor.dash();
        }

        if(Button.C.justPressed()){
            switch(powerReady){
                case 0:
                    powerReady = -1;
                    quantity = 0;
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
    
    void updatePower(){
        if(quantity >= 10){
            powerReady++;
            quantity = 0;
        }
        if(powerReady >= 3) powerReady = 0;
    }
    
    void updateCoffeeDrops(){
        int reset = 0;
        for(int x = 0; x < 6; x++){
            coffees[x]-= 1+speed;
            if(coffees[x] < -10) reset++;
        }
        if(reset > 5){
            int y = Math.random(70, 120);
            generateCoffeeDrops(y);
        }
    }
    void drawCoffeeDrops(){
        for(int x : coffees){
            screen.fillRect(x, coffeeY, 4, 6, 10);
        }
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
            screen.drawCircle(10*j+10, 163, 2, 5);
        }
    }
    
    void drawJumps(){
        for(int i = 0; i < torJump; i++){
            screen.drawCircle(4*i+9, 170, 1, 10);
        }
    }
}
