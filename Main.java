import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.input.Button;
import femto.palette.GrungeShift;
import femto.font.TIC80;

import entities.Enemy;

import Tor;

class Main extends State {

    HiRes16Color screen; // the screenmode we want to draw with

    boolean jump = false, dashing = false, dashReady = true;
    Tor tor;

    float dxs, dys, torJump, torGravity;
    int timer, dashTime, dashCharge, speed, torSpeed, powerReady;
    
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
        timer = 0;
        speed = 0;
        torSpeed = 0;
        torGravity = 0.2;

        quantity = 0;
        powerReady = -1;
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
        
        timer++;
        
        if(jump && !dashing){
            dys += torGravity;//Gravity
            if(Button.Down.isPressed()){
                dys += 0.2;
            }
        }else{
            dys = 0;
        }
        
        if(tor.y >= 100){
            tor.y = 100;
            jump = false;
            if(!dashing)tor.run();
        }

        if( Button.A.justPressed() && !jump) {
            dys = -4+torJump;
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
                    torJump-=0.2;
                    powerReady = -1;
                    break;
                case 2:
                    torGravity -= 0.05;
                    if(torGravity <= 0.0)torGravity = 0.01;
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

        //START Draw BG
        screen.fillRect(0, 128, 230, 10, 1);
        screen.fillRect(0, 138, 230, 50, 0);
        //END Draw BG
        if(dashing)speed = 2;
        else speed = 0;
        
        tor.y += dys;
        tor.draw(screen); // Animation is updated automatically
     
        
        screen.drawRect(0, 170, dashCharge+1, 4, 5);
        screen.fillRect(1, 171, dashTime, 3, 6);
        
        updatePower();
        checkCoffeeCollect();
        updateCoffeeDrops();
        drawCoffeeDrops();

        drawUpgrades();
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    
    void checkCoffeeCollect(){
        for(int x = 0; x < 6; x++){
            if(tor.x < coffees[x] + 4 && tor.x + 24 > coffees[x] &&
                tor.y < coffeeY + 6 && tor.y + 24 > coffeeY)
            {
                System.out.println("Collision Detected: " + quantity);
                quantity++;
                coffees[x] = -20;
            }
        }
    }
    
    void updatePower(){
        if(quantity >= 10){
            powerReady++;
            quantity = 0;
        }
        if(powerReady >= 5) powerReady = 0;
    }
    
    void updateCoffeeDrops(){
        int reset = 0;
        for(int x = 0; x < 6; x++){
            coffees[x]-= 1+speed;
            if(coffees[x] < 0 && coffees[x] > -10) coffees[x] = 221;
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
        for(int i = 0; i < 6; i++){
            screen.drawRect(32*i+8, 150, 20, 6, 10);
        }
        screen.fillRect(32*powerReady+9, 152, 18, 4, 11);
    }
}
