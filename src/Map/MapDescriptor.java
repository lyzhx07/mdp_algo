package Map;

public class MapDescriptor {

    public MapDescriptor() {

    }

    /**
     * Right pad "0" to the binary string so that its length is in multiple of 8 (as required)
     * @param biStr
     * @return
     */
    private String rightPadTo8(String biStr) {
        int check = biStr.length() % 8;
        if (check != 0) {
            int to_pad = 8 - check;
            System.out.println("Length of binary string not divisible by 8.");
            System.out.printf("Length of string: %d, Right Padding: %d\n", biStr.length(), to_pad);
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < to_pad; i++) {
                padding.append('0');
            }
            biStr += padding.toString();
        }
        return biStr;
    }

    /**
     * Left pad "0" to the binary string until its length is multiple of 4 (one hex = 4 bits)
     * @param biStr
     * @return
     */
    private String leftPadTo4(String biStr) {
        int check = biStr.length() % 4;
        if (check != 0) {
            int to_pad = 4 - check;
            System.out.println("Length of binary string not divisible by 4.");
            System.out.printf("Length of string: %d, Left Padding: %d\n", biStr.length(), to_pad);
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < to_pad; i++) {
                padding.append('0');
            }
            biStr = padding.toString() + biStr;
        }
        return biStr;
    }

    /**
     * Convert 8-bit binary to 2-bit hex
     * @param biStr 8-bit binary string
     * @return 2-bit hex string
     */
    private String biToHex(String biStr) {
        int dec = Integer.parseInt(biStr, 2);
        String hexStr = Integer.toHexString(dec);
        // pad left 0 if length is 1
        if (hexStr.length() < 2) {
            hexStr = "0" + hexStr;
        }
        return hexStr;
    }

    /**
     * Convert the entire hex string to binary string
     * @param biStr
     * @return
     */
    private String hexToBi(String hexStr) {
        String biStr = "";
        String tempBiStr = "";
        int tempDec;
        for (int i = 0; i < hexStr.length(); i++) {
            tempDec = Integer.parseInt(Character.toString(hexStr.charAt(i)), 16);
            tempBiStr = Integer.toBinaryString(tempDec);
            biStr += leftPadTo4(tempBiStr);
        }
        return biStr;
    }

    public String generateMDFString1(Map map) {
        StringBuilder MDFcreator1 = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        temp.append("11");
        for (int r = 0; r < MapConstants.MAP_HEIGHT; r++) {
            for (int c = 0; c < MapConstants.MAP_WIDTH; c++) {
                temp.append(map.getCell(r, c).isExplored() ? '1':'0');
                // convert to hex every 8 bits to avoid overflow
                if(temp.length() == 8) {
                    MDFcreator1.append(biToHex(temp.toString()));
                    temp.setLength(0);
                }
            }
        }
        // last byte
        temp.append("11");
        MDFcreator1.append(biToHex(temp.toString()));

        return MDFcreator1.toString();
    }

    public String generateMDFString2(Map map) {
        StringBuilder MDFcreator2 = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int r = 0; r < MapConstants.MAP_HEIGHT; r++) {
            for (int c = 0; c < MapConstants.MAP_WIDTH; c++) {
                if (map.getCell(r, c).isExplored()) {
                    temp.append(map.getCell(r, c).isObstacle() ? '1' : '0');
                    if (temp.length() == 8) {
                        MDFcreator2.append(biToHex(temp.toString()));
                        temp.setLength(0);
                    }
                }
            }
        }

        // last byte
        if(temp.length() % 8 != 0) {
            // right pad to 8
            String tempBiStr = rightPadTo8(temp.toString());
            MDFcreator2.append(biToHex(tempBiStr));
        }

        return MDFcreator2.toString();

    }

    public void loadMDFString1(String MDFstr1, Map map) {
        String expStr = hexToBi(MDFstr1);
        int index = 2;
        for (int r = 0; r < MapConstants.MAP_HEIGHT; r++) {
            for (int c = 0; c < MapConstants.MAP_WIDTH; c++) {
                if (expStr.charAt(index) == '1') {
                    map.getCell(r, c).setExplored(true);
                }
                index++;
            }
        }
        System.out.println("index: " + index);
    }

    public void loadMDFString2(String MDFstr2, Map map) {
        String obsStr = hexToBi(MDFstr2);
        int index = 0;
        for (int r = 0; r < MapConstants.MAP_HEIGHT; r++) {
            for (int c = 0; c < MapConstants.MAP_WIDTH; c++) {
                Cell cell = map.getCell(r, c);
                if (cell.isExplored()) {
                    if (obsStr.charAt(index) == '1') {
                        cell.setObstacle(true);
                    }
                    // create virtual wall
                    map.setVirtualWall(cell);
                    index++;
                }
            }
        }
    }

    // MDF Testing
    public static void main(String[] args) {
        Map m = new Map();
        String str1 = "ffc07f80ffe1ffc3ffc7fff1fe03fc03f807f80ffe1ffc3ff87ff007e00fc01f803f003e007f";
        String str2 = "0303000000007c07c0400001000000007c0000000000";

        MapDescriptor mdfCoverter = new MapDescriptor();
        mdfCoverter.loadMDFString1(str1, m);
        mdfCoverter.loadMDFString2(str2, m);

        String str1_test = mdfCoverter.generateMDFString1(m);
        String str2_test = mdfCoverter.generateMDFString2(m);

        System.out.println(str1);
        System.out.println(str1_test);
        System.out.println(str2);
        System.out.println(str2_test);


    }

}
