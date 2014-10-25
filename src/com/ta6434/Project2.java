package com.ta6434;

import java.io.*;
import java.util.Scanner;

public class Project2 {

    public static void main(String[] args) throws IOException {

        Scanner scan = new Scanner(System.in);

        System.out.print("Enter file name to compress: ");
        String fileName = scan.nextLine();

        String originalFileName = fileName.split("\\.")[0];
        String originalFileExt = fileName.split("\\.")[1];

        File originalFile = new File(fileName);
        File compressedFile = new File(originalFileName + ".huff" + "." + originalFileExt);

        System.out.println("Compressing " + fileName + "..");

        FrequencyTable freq = getFrequencies(originalFile);
        freq.increment(256);  // EOF symbol gets a frequency of 1
        CodeTree code = freq.buildCodeTree();
        CanonicalCode canonCode = new CanonicalCode(code, 257);
        code = canonCode.toCodeTree();  // Replace code tree with canonical one. For each symbol, the code value may change but the code length stays the same.

        // Read input file again, compress with Huffman coding, and write output file
        InputStream in = new BufferedInputStream(new FileInputStream(originalFile));
        BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(compressedFile)));
        try {
            writeCode(out, canonCode);
            compress(code, in, out);
        } finally {
            out.close();
            in.close();
        }

        //test decompress so we can check whether the compression is correct
        File decompressedFile = new File(originalFileName + ".dehuff" + "." +  originalFileExt);
        decompress(compressedFile, decompressedFile);

    }

    private static FrequencyTable getFrequencies(File file) throws IOException {
        FrequencyTable freq = new FrequencyTable(new int[257]);
        InputStream input = new BufferedInputStream(new FileInputStream(file));
        try {
            while (true) {
                int b = input.read();
                if (b == -1)
                    break;
                freq.increment(b);
            }
        } finally {
            input.close();
        }
        return freq;
    }


    static void writeCode(BitOutputStream out, CanonicalCode canonCode) throws IOException {
        for (int i = 0; i < canonCode.getSymbolLimit(); i++) {
            int val = canonCode.getCodeLength(i);
            // For this file format, we only support codes up to 255 bits long
            if (val >= 256)
                throw new RuntimeException("The code for a symbol is too long");

            // Write value as 8 bits in big endian
            for (int j = 7; j >= 0; j--)
                out.write((val >>> j) & 1);
        }
    }


    static void compress(CodeTree code, InputStream in, BitOutputStream out) throws IOException {
        HuffmanEncoder enc = new HuffmanEncoder(out);
        enc.codeTree = code;
        while (true) {
            int b = in.read();
            if (b == -1)
                break;
            enc.write(b);
        }
        enc.write(256);  // EOF
    }

    static void decompress(File compressedFile, File decompressedFile) throws IOException {

        BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(compressedFile)));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(decompressedFile));

        try {

            int[] codeLengths = new int[257];
            for (int i = 0; i < codeLengths.length; i++) {
                // For this file format, we read 8 bits in big endian
                int val = 0;
                for (int j = 0; j < 8; j++)
                    val = val << 1 | in.readNoEof();
                codeLengths[i] = val;
            }
            CanonicalCode canonCode = new CanonicalCode(codeLengths);

            CodeTree code = canonCode.toCodeTree();
            HuffmanDecoder dec = new HuffmanDecoder(in);
            dec.codeTree = code;
            while (true) {
                int symbol = dec.read();
                if (symbol == 256)  // EOF symbol
                    break;
                out.write(symbol);
            }
        } finally {
            out.close();
            in.close();
        }

    }

}
