import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.GrungeShift;
import femto.font.TIC80;

import Tor;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    boolean jump = false, dashing = false, dashReady = true;
    Tor tor;
    
    float dxs, dys, torGravity, E1, E2;
    int dashTime, dashCharge, speed, torSpeed, powerReady, torHits, torMaxJump, torJump;
    
    int quantity, coffeeY;
    int[] coffees;

    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        screen = new HiRes16Color(GrungeShift.palette(), TIC80.font());

        tor = new Tor();
        tor.y = 100;
        tor.x = 32;
        tor.run();

        dashTime = 0;
        dashCharge = 50;
        speed = 1;
        torSpeed = 0;
        torGravity = 0.2;
        torHits = 3;
        torMaxJump = 1;
        torJump = 1;

        quantity = 0;
        powerReady = -1;
        E1 = Math.random(220, 400);
        E2 = Math.random(220, 400);
        int y = Math.random(70, 120);
        generateCoffeeDrops(y);
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
        screen.clear(7);
        
        //START Draw BG
        screen.fillRect(0, 128, 230, 10, 8);
        screen.fillRect(0, 138, 230, 50, 0);
        //END Draw BG
        if(dashing)speed = 3;
        else speed = 1;
        
        updateTor();

        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
        
        E1-=1+speed;
        E2-=1+speed;
        
        if(E1 <= -10) E1 = Math.random(220, 400);
        if(E2 <= -10) E2 = Math.random(220, 400);
        
        //enemies
        screen.fillCircle((int)E1, 80, 4, 5);
        screen.fillCircle((int)E2, 90, 4, 5);
        

        drawPowerBox();
        drawTorHits();
        
        updatePower();
        checkCoffeeCollect();
        updateCoffeeDrops();
        drawCoffeeDrops();

        drawUpgrades();
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
    void checkCoffeeCollect(){
        //debug hit box
        //screen.drawRect((int)tor.x, (int)tor.y+4, (int)tor.width(), (int)tor.height() - 4, 5);
        for(int x = 0; x < 6; x++){
            if(tor.x < coffees[x] + 4 && tor.x + tor.width() > coffees[x] &&
                tor.y + 4 < coffeeY + 6 && tor.y + 4 + tor.height() - 4 > coffeeY)
            {
                quantity++;
                coffees[x] = -20;
            }
        }
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
        
        if(tor.y >= 100){
            tor.y = 100;
            jump = false;
            torJump = torMaxJump;
            if(!dashing)tor.run();
        }

        //INPUT
        if( Button.A.justPressed() && (!jump || torJump > 0)) {
            dys = -3.5;
            torJump--;
            jump = true;  
            tor.jump();
        }
        
        if(Button.B.isPressed() && dashTime > 0 && dashReady){
            dashTime--;
            dashing = true;
            tor.dash();
        }
        
        if(Button.C.justPressed()){
            switch(powerReady){
                case 0:
                    powerReady = -1;
                    quantity = 0;
                    dashCharge += 10;
                    break;
                case 1:
                    torMaxJump++;
                    torJump = torMaxJump;
                    powerReady = -1;
                    break;
                case 2:
                    if(torHits < 6) torHits++;
                    powerReady = -1;
                    break;
                default:
                    break;
            }
        }
        
        if(!Button.B.isPressed()){
            if(dashing){
                dashing = false;
                tor.run();
            }
        }
        //END Input
        
        if(!dashReady){
            dashTime++;
            if(dashTime >= dashCharge){
                dashTime = dashCharge;
                dashReady = true;
            }
        }
        
        if(dashTime <= 0 ){
            dashing = false;
            dashReady = false;
            tor.run();
        }
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
            screen.drawCircle(10*j+10, 164, 2, 5);
        }
    }
}
