package com.HeiseiChain.HeiseiChain.model;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Blockchain {
    private List<Block> chain;
    private List<Transaction> currentTransactions;

    public Blockchain() {
        chain = new ArrayList<>();
        currentTransactions = new ArrayList<>();
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(new ArrayList<>(), "0"); // Genesis block has no previous hash
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addTransaction(Transaction transaction) {
        currentTransactions.add(transaction);
        if (currentTransactions.size() >= 1) {  // Add block when the list reaches a predefined size
            addBlock(new Block(currentTransactions, getLatestBlock().getHash()));
            currentTransactions = new ArrayList<>();  // Reset transactions list after adding block
        }
        HeiseiChain.processUTXOs(transaction);
    }

    public void addBlock(Block newBlock) {
        newBlock.setPreviousHash(getLatestBlock().getHash());
        newBlock.setHash(newBlock.calculateHash());
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Validate hash and previous hash
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() {
        return chain;
    }

    public String displayHTML(Map<String, Wallet> walletDatabase) {
        StringBuilder data = new StringBuilder();
        data.append("<div class=\"container\">");

        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            data.append("<div class=\"block\">")
                    .append("<div class=\"block-header\">Block ").append(i).append("</div>")
                    .append("<div class=\"block-body\">")
                    .append("<p><strong>Hash:</strong> ").append(block.getHash()).append("</p>")
                    .append("<p><strong>Previous Hash:</strong> ").append(block.getPreviousHash()).append("</p>")
                    .append("<p><strong>Timestamp:</strong> ").append(block.getFormattedTimestamp()).append("</p>");

            if (block.getTransactions() != null && !block.getTransactions().isEmpty()) {
                data.append("<div class=\"transactions\">");
                for (Transaction transaction : block.getTransactions()) {
                    data.append("<div class=\"transaction\">")
                            .append("<p><strong>Sender:</strong> ").append(findUserByPublicKey(transaction.getSender(), walletDatabase)).append("</p>")
                            .append("<p><strong>Recipient:</strong> ").append(findUserByPublicKey(transaction.getRecipient(), walletDatabase)).append("</p>")
                            .append("<p><strong>Value:</strong> ").append(transaction.getValue()).append("</p>")
                            .append("<p><strong>Metadata:</strong> ").append(transaction.getMetadata()).append("</p>")
                            .append("</div>");
                }
                data.append("</div>");
            } else {
                data.append("<div class=\"transactions\">")
                        .append("<p class=\"no-transactions\">No transactions in this block.</p>")
                        .append("</div>");
            }
            data.append("</div></div>");
        }

        data.append("</div>");
        return data.toString();
    }

    private String findUserByPublicKey(PublicKey key, Map<String, Wallet> walletDatabase) {
        for (Map.Entry<String, Wallet> entry : walletDatabase.entrySet()) {
            if (entry.getValue().publicKey.equals(key)) {
                return entry.getKey();
            }
        }
        return "Unknown User";
    }



}