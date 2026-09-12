public class Lab1{

    public static void main(String[] args) {
        short[] w = {5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        double[] x = new double[20];
        for (int i = 0; i < x.length; i++) {
            x[i] = -15.0 + (12.0 - (-15.0)) * Math.random();
            System.out.print(x[i] + " ");
        }
        System.out.println();
        out(matrix(x, w, w.length, x.length));
    }

    public static double[][] matrix(double[] masX, short[] masW ,int first, int second) {
        double[][] wx = new double[first][second];
        for (int i = 0; i < wx.length; i++) {
            for (int j = 0; j < wx[i].length; j++) {
                switch (masW[i]) {
                    case 7 -> wx[i][j] = Math.cos(Math.tan(Math.pow(masX[j], masX[j] * (masX[j] + 0.25))));
                    case 9, 10, 11, 13, 14, 15, 16, 18 -> wx[i][j] = Math.cos(Math.log(Math.abs(masX[j])));
                    default -> {
                        double ground = 1 / (Math.exp(2 * ((Math.pow(((Math.abs(masX[j]) + 1) / Math.abs(masX[j])), 2)) + 1) / Math.PI / 2));
                        double sky = Math.pow(Math.atan(Math.exp(-Math.abs(masX[j]))), (Math.cbrt(Math.pow(2 / (masX[j] - 1.0 / 3.0), masX[j]))) + 1.0 / 2.0);
                        wx[i][j] = Math.atan(Math.pow(ground, sky));
                    }
                }
            }
        }
        return wx;
    }

    public static void out(double[][] wx) {
        for(double[] row: wx){
            for (double value: row){
                System.out.printf("%7.3f ", value);
            }
            System.out.println(" ");
        }
    }
}
