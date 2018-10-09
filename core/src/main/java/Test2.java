import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

/**
 * User: Taoz
 * Date: 8/22/2018
 * Time: 5:51 PM
 */
public class Test2 {


  public static void main(String[] args) throws InterruptedException {


    System.out.println("hello, java.");

    int w = 400;
    int h = 300;

    BufferedImage image =
      new BufferedImage(
        w,
        h,
        BufferedImage.TYPE_INT_RGB
      );


    Raster r = image.getRaster();
    DataBuffer db = r.getDataBuffer();


    Thread.sleep(200);

    long t1 = System.currentTimeMillis();

    int[] arr = null;
    for (int i = 0; i <1 ; i++) {
      arr = image.getRGB(0, 0, w, h, null, 0, w);
      if((long)i > System.currentTimeMillis()) {
        arr[0] = 0;
        System.out.println(".............:");
      }
    }

    long t2 = System.currentTimeMillis();



/*

    int[] arr = new int[w * h];
    image.getRGB(0, 0, w, h, arr, 0, w);
*/


    System.out.println("type:" + db.getDataType() );
    System.out.println("size:" + db.getSize() );


    System.out.println("arr size:" + arr.length );
    System.out.println("arr:" + arr[0] );
    System.out.println("arr:" + arr[5] );
    System.out.println("t:" + (t2 - t1) );






  }


}
