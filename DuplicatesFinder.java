/* Things to note:
 - If 'Output' folder already exists, it will be deleted and create new.
 - It will only read .png files, ignore rest.
 - No longer doing reportResults.txt
 - No stable sorting
*/
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.FileVisitOption;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DuplicatesFinder {

    public static void main(String[] args) throws Exception {

        try {
            int totalMatches = 0;
            int totalGroups = 0;

            // create a new output folder
            createFolder("Output");

            // open read folder, collect all files.
            File folder = new File("./Examine");
            File[] files = folder.listFiles();

            // debug
            //System.out.println(files.length + " files to examine.");

            int i = 0; // index of current image 
            int l = files.length; // will be -1 everytime we move an image

            while (i < l - 1) {

                // take current image
                File currFile = files[i];

                if (!currFile.getName().toLowerCase().endsWith(".png")) {
                    i++;
                    continue;
                }

                boolean isUnique = true;

                // debug
                //System.out.println("currFile: " + currFile.getName());

                int groupMember = 1;

                for (int j = i + 1; j < l; j++) {

                    File compareFile = files[j];

                    if (!compareFile.getName().toLowerCase().endsWith(".png")) {
                        continue;
                    }

                    // debug
                    //System.out.println("   compareFile: " + compareFile.getName());

                    if (checkDuplicates(currFile, compareFile)) {

                        // debug
                        //System.out.println("   !!!match!!!");

                        isUnique = false;

                        // move 2nd image to output folder
                        Path sourcePath = Path.of("./Examine", compareFile.getName());
                        Path destinationPath = Path.of("./Output", "match" + (totalGroups + 1) + "-" + groupMember + "_" + compareFile.getName());

                        groupMember++;

                        try {
                            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            System.out.println("Failed to move the file: " + e.getMessage());
                        }

                        // correct the array
                        shiftLeft(files, j, l);
                        l--;

                        // take count
                        totalMatches++;
                    }
                }

                if (!isUnique) {
                    // move image to output folder
                    Path sourcePath = Path.of("./Examine", currFile.getName());
                    Path destinationPath = Path.of("./Output", "match" + (totalGroups + 1) + "-" + groupMember + "_" + currFile.getName());
                    try {
                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("Failed to move the file: " + e.getMessage());
                    }

                    // correct the array
                    shiftLeft(files, i, l);
                    l--;

                    // take count
                    totalMatches++;
                    totalGroups++;
                } else i++;
            }


            System.out.println("Operation Complete\nTotal " + totalMatches + " items matching(" + totalGroups + " groups)");

        } catch (Exception e) {
            System.out.println("Some error occured. Operation broke");
        }
    }



    public static void shiftLeft(File[] arr, int StartIndex, int vLength) {

        // shift elements to the left, starting from StartIndex
        for (int i = StartIndex; i < vLength - 1; i++)
            arr[i] = arr[i + 1];
    }


    public static boolean checkDuplicates(File f1, File f2) throws Exception {

        BufferedImage img1 = ImageIO.read(f1);
        BufferedImage img2 = ImageIO.read(f2);

        // check if ! same dimensions
        if (img1.getHeight() != img2.getHeight() ||
            img1.getWidth() != img2.getWidth()) {
            return false;
        }

        // check if colors matching, pixel by pixel
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {

                int p1 = img1.getRGB(x, y);
                int p2 = img2.getRGB(x, y);

                Color color1 = new Color(p1, true);
                Color color2 = new Color(p2, true);

                if (!color1.equals(color2)) {
                    return false;
                }
            }
        }
        return true;
    }


    public static void createFolder(String folderPath) throws IOException {

        Path folder = Paths.get(folderPath);

        if (Files.exists(folder)) {
            deleteFolder(folderPath); // delete the existing folder and its content
        }

        Files.createDirectory(folder); // create the new folder
    }


    public static void deleteFolder(String folderPath) throws IOException {

        Path folder = Paths.get(folderPath);

        if (Files.exists(folder)) {
            Files.walk(folder, FileVisitOption.FOLLOW_LINKS)
                .sorted((a, b) -> b.compareTo(a)) // sort in reverse order for proper deletion
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.out.println("Failed to delete: " + path + " - " + e.getMessage());
                    }
                });
        }
    }

}