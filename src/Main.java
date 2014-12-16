import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;


public class Main {
	
	public static void main(String[] args){
		Random rand = new Random();

		runSimulation("Louissianna", 16900, 144, 26, 2, 10);
		runSimulation("Massachusettes", 7200, 181, 26, 3, 10);
		runSimulation("California", 16300, 127, 31, 2, 10); //31.3 m/s
		
		
		runSimulation("Test50", 20000, 50, 22, 3, 10); //80kph = 22.222 m/s
		runSimulation("Test100", 20000, 100, 22, 3, 10);
		runSimulation("Test200", 20000, 200, 22, 3, 10);
		
		if(true) return;
		
		//Road r = buildRoad(10 * 1000, 1000, 3);

		//Road r = buildRoad(7200, 181, 26, 3); //97 k/h = 26m/s.  Mass Road
		Road r = buildRoad(16900, 144, 26, 2); //97 k/h = 26m/s.  LA road
		
		r.renderRoad("Test.png");
		r.renderRoadAdvanced("Test2.png");

		System.out.println("Total Vehicles: " + r.vehicles.size());
		System.out.println("Total Accidents: " + r.collisions.size());
		
		int[] laneCounts = r.countRoadUse();
		for(int i = 0; i < laneCounts.length; i++){
			System.out.println("Cars in Lane " + i + ": " + laneCounts[i]);
		}
		
		int[] accidentCounts = r.countAccidents();
		for(int i = 0; i < laneCounts.length; i++){
			System.out.println("Accidents in Lane " + i + ": " + accidentCounts[i]);
		}
		
		for(int i = 0; i < laneCounts.length; i++){
			System.out.println("Safety of Lane " + i + ": " + (100 * ((laneCounts[i] == 0) ? 1 : (1 - (double)accidentCounts[i] / (double)laneCounts[i]))) + "%");
		}
		
	}
	
	static int chooseWeighted(int choice, int[] weights){
		for(int i = 0; i < weights.length; i++){
			if(choice < weights[i]){
				return i;
			}
			choice -= weights[i];
		}
		return -1;
	}
	
	static int sum(int[] weights){
		int sum = 0;
		for(int i = 0; i < weights.length; i++){
			sum += weights[i];
		}
		return sum;
	}
	
	static void multiply(double[] a, double s){
		for(int i = 0; i < a.length; i++){
			a[i] *=s;
		}
	}
	
	static double[] mean(int[][] weights){
		double[] ret = new double[weights[0].length];
		
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < ret.length; j++){
				ret[j] += weights[i][j];
			}
		}
		
		for(int i = 0; i < ret.length; i++){
			ret[i] /= weights.length;
		}
		return ret;
	}

	public static void runSimulation(String name, int length, int cars, int speedLimit, int lanes, int runs){

		//Create output directory
		new java.io.File(name).mkdir();
		
		String[] dataNames = new String[]{"vehicles", "accidents", "load", "avgkph"};
		int[][][] data = new int[4][runs][];
		
		for(int i = 0; i < runs; i++){
			Road r = buildRoad(length, cars, speedLimit, lanes);

			data[0][i] = r.countRoadUse();
			data[1][i] = r.countAccidents();
			data[2][i] = r.countUsedSpaces();
			data[3][i] = r.avgSpeed();

			r.renderRoadAdvanced(name + "/" + i + ".png");
			
			//TODO average speed
		}
		
		double[][] means = new double[data.length][];
		for(int i = 0; i < data.length; i++){
			means[i] = mean(data[i]);
		}
		
		//Transforms
		multiply(means[2], 1.0 / length); //convert to a fraction of the total road length
		multiply(means[3], 1.0 / 0.277777777777778); //convert to kph
		
		//Write out averages
		try{
			String outName = name + "/" + "summary.txt";
			OutputStream out = new FileOutputStream(outName);
			
			String outString = "";
			
			outString += "# " + "Road statistics for " + name + ": " + (length / 1000.0) + "km, " + cars + " cars, " + speedLimit + " kph zone, " + lanes + " lanes" + "\n";
			
			for(int i = 0; i < data.length; i++){
				outString += dataNames[i];
				for(int j = 0; j < means[i].length; j++){
					outString += ", " + means[i][j];
				}
				outString += "\n";
			}
			
			out.write(outString.getBytes());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
    //ms 220522, sm 195434, truck 432446, SUV 333977, 
	public static final int[] carWeights = new int[]{0, 0, 195434, 220522, 432446, 5000, 1000, 1000, 2000};
	public static final String[] types = new String[]{null, null, "Micro", "Car", "Pickup", "Van", "Short Bus", "Full Bus", "18-Wheeler"};
	
	public static double DANGER_FREQ = .01;
	
	public static Road buildRoad(int length, int cars, int speedLimit, int lanes){
		
		int sumWeights = sum(carWeights);
		
		Road r = new Road(10 * 1000, lanes);
		
		Random rand = new Random(0xf1580e6c70d539l);
		
		for(int i = 0; i < cars; i++){
			
			int carType = chooseWeighted(rand.nextInt(sumWeights), carWeights);
			
			boolean dangerous = (rand.nextFloat() < DANGER_FREQ);
			
			int carLen = carType;
			String carName = types[carLen];
			
			int speed = rand.nextInt(speedLimit / 4 * 2 + 5) + (3 * speedLimit) / 4 - carLen / 2;
			
			if(dangerous){
				speed += 5;
			}
			
			Vehicle v = new Vehicle();
			v.length = carLen;
			v.speed = speed;
			v.name = carName;
			v.dangerous = dangerous;
			
			for(int j = 0; j < 8; j++){
				if(r.putCar(v)) break;
			}
		}
		return r;
	}
	
	//TODO simulate some highways, get some images, compare to ideal and real world data, write "multisimulator" to get more accurate statistics, use lane occupation, average speeds.
}
