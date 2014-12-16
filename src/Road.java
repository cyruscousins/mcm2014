import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;


public class Road {
	
	Random rand = new Random();
	
	List<Vehicle> vehicles = new ArrayList<Vehicle>();
	List<Vehicle> collisions = new ArrayList<Vehicle>();
	
	public static final int EMPTY = 0, CAR = 1, BEHINDCAR = 2, CONSTRUCTION = 3, ICE = 4, ACCIDENT = 5, BACK1 = 6, BACK2 = 7;
	
	public static int SPEEDDROP = 5;
	
	int[] colors = new int[]{0xffffff, 0x000000, 0xcccccc, 0x222222, 0xaaffbb, 0xffccaa, 0x555555, 0x888888};
	
	int length, buffer, lanes;
	int[][] data;
	public Road(int length, int lanes){
		this.length = length;
		this.lanes = lanes;
		data = new int[length][lanes];
	}
	
	private boolean checkFree(int x, int y, int len){
		for(int i = 0; i < len; i++){
			if(data[y-i][x] != EMPTY) return false;
		}
		return true;
	}
	
	private boolean checkForCar(int y, int x, int len){
		for(int i = 0; i < len; i++){
			if(data[y-i][x] == CAR) return true;
		}
		return false;
	}
	
	
	int avgclen = 4; //TODO try 4.
	int carlen3s(int speed, int length){
		//System.out.println("S: " + speed + ", len " + (speed * 3 + length));
		return speed * 3 + length;
	}
	
	//Attempt to place a car into this model.
	public boolean putCar(Vehicle vehicle){
		if(vehicle.dangerous){
			return putDangerCar(vehicle);
		}
		else{
			return putSafeCar(vehicle);
		}
	}
	
	public boolean putDangerCar(Vehicle vehicle){
		int x = rand.nextInt(lanes) + 1;
		if(x >= lanes) x = lanes - 1;

		vehicle.lane = x;
		
		int y = rand.nextInt(length - vehicle.length) + vehicle.length;
		
		if(checkForCar(y, x, vehicle.length)){
			
			for(int i = 0; i < vehicle.length; i++){
				if(y - i < 0){
					break;
				}
				data[y - i][x] = ACCIDENT;
			}
			
			//A collision occurs.
			vehicles.add(vehicle);
			collisions.add(vehicle);
			return true;
		}
		else{
			int vlenExtended = carlen3s(vehicle.speed, vehicle.length);

			for(int i = 0; i < vehicle.length; i++){
				if(y - i < 0){
					break;
				}
				data[y - i][x] = CAR;
			}
			
			for(int i = vehicle.length; i < vlenExtended; i++){
				if(y - i < 0 || data[y - i][x] != EMPTY){
					break;
				}
				data[y - i][x] = BEHINDCAR;
			}
			
			vehicles.add(vehicle);
			return true;
		}
		
	}
	
	public boolean putSafeCar(Vehicle vehicle){
		//Pick random coordinates
		int y = rand.nextInt(length);
		
		int vlen = vehicle.length;
		int vlenExtended = carlen3s(vehicle.speed, vehicle.length);
		
		while(true){
			
			//Try to place
			
			if(y < vlenExtended){
				return false;
			}
			
			for(int x = 0; x < lanes; x++){
				if(checkFree(x, y, vlenExtended)){
					
					for(int c = 0; c < vlen; c++){
						data[y - c][x] = CAR;
					}
					for(int c = vlen; c <  vlenExtended; c++){
						if(y - c > 0){
							data[y - c][x] = BEHINDCAR;
						}
						
					}
					
					vehicle.lane = x;
					vehicles.add(vehicle);
					return true;
				}
			}
			y--;
			
			vehicle.speed -= SPEEDDROP;
		}
	}
	
	void printRoad(OutputStream out) throws IOException{
		//Transpose the order.
		for(int y = length - 1; y >= 0; y--){
			for(int x = lanes - 1; x >= 0; x--){
				if(data[y][x] == 0){
					out.write(' ');
				}
				else{
					out.write('0' + data[y][x]);
				}
			}
			out.write('\n');
		}
		
	}
	
	void renderRoad(String s){
		BufferedImage i = new BufferedImage(lanes, length, BufferedImage.TYPE_INT_RGB);
		
		for(int y = 0; y < length; y++){
			for(int x = 0; x < lanes; x++){
				i.setRGB(lanes - x - 1, length - y - 1, colors[data[y][x]]);
			}
		}
		
		//RenderedImage r = new Rend
		
		try{
			FileOutputStream o = new FileOutputStream(s);
			ImageIO.write(i, "png", o);
			o.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	int tWidth = 6;
	int gSpaceB = 100;
	int gSpaceS = 10;
	public void renderRoadAdvanced(String s){
		BufferedImage i = new BufferedImage(lanes * 3 + tWidth + 2, length * 3, BufferedImage.TYPE_INT_RGB);
		
		java.awt.Graphics g = i.getGraphics();
		g.setColor(java.awt.Color.white);
		g.fillRect(0, 0, i.getWidth(), i.getHeight());
		
		for(int y = 0; y < length; y++){
			for(int x = 0; x < lanes; x++){
				int color = colors[data[y][x]];
				if(data[y][x] == BEHINDCAR && y < length - 1 && data[y+1][x] == CAR){
					color = colors[BACK1];
				}
				else if(data[y][x] == BEHINDCAR && y < length - 2 && data[y+2][x] == CAR){
					color = colors[BACK2];
				}
				i.setRGB((lanes - x - 1) * 3 + 0, (length - y - 1) * 3, color);
				i.setRGB((lanes - x - 1) * 3 + 1, (length - y - 1) * 3, color);
				i.setRGB((lanes - x - 1) * 3 + 0, (length - y - 1) * 3 + 1, color);
				i.setRGB((lanes - x - 1) * 3 + 1, (length - y - 1) * 3 + 1, color);
				i.setRGB((lanes - x - 1) * 3 + 0, (length - y - 1) * 3 + 2, color);
				i.setRGB((lanes - x - 1) * 3 + 1, (length - y - 1) * 3 + 2, color);
			}
			if(y % gSpaceB == 0){
				for(int j = 0; j < tWidth; j++)
				{
					
					i.setRGB(lanes * 3 + j + 2, (length - y - 1) * 3 + 2, 0x000000);
					if((length - y - 1) * 3 + 3 < length * 3)
						i.setRGB(lanes * 3 + j + 2, (length - y - 1) * 3 + 3, 0x000000);
				}
			}
			else if (y % gSpaceS == 0){
				for(int j = tWidth / 2; j < tWidth; j++)
				{
					i.setRGB(lanes * 3 + j + 2, (length - y - 1) * 3 + 2, 0x000000);
					if((length - y - 1) * 3 + 3 < length * 3)
						i.setRGB(lanes * 3 + j + 2, (length - y - 1) * 3 + 3, 0x000000);
				}
			}
			int borderGray = 0x444444;
			i.setRGB(lanes * 3, y * 3 + 0, borderGray);
			i.setRGB(lanes * 3, y * 3 + 1, borderGray);
			i.setRGB(lanes * 3, y * 3 + 2, borderGray);
		}
		
		//RenderedImage r = new Rend
		
		try{
			FileOutputStream o = new FileOutputStream(s);
			ImageIO.write(i, "png", o);
			o.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public int[] countRoadUse(){
		int[] ret = new int[lanes];
		
		for(int i = 0; i < vehicles.size(); i++){
			ret[vehicles.get(i).lane]++;
		}
		return ret;
	}
	
	public int[] countAccidents(){
		int[] ret = new int[lanes];
		
		for(int i = 0; i < collisions.size(); i++){
			ret[collisions.get(i).lane]++;
		}
		return ret;
	}
	
	public int[] countUsedSpaces(){
		int[] ret = new int[lanes];
		
		for(int y = 0; y < length; y++){
			for(int x = 0; x < lanes; x++){
				if(data[y][x] != EMPTY){
					ret[x]++;
				}
			}
		}
		
		return ret;
		
	}
	
	public int[] avgSpeed(){
		int s = 0;
		for(int i = 0; i < vehicles.size(); i++){
			s += vehicles.get(i).speed;
		}
		return new int[]{s / vehicles.size()};
	}
}
