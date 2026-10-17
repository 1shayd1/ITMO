import dishes.*;
import exceptions.DoNotLikeGift;
import exceptions.GiftOverflow;
import gifts.*;
import heroes.*;

import java.util.ArrayList;

public class StoryText {

    ArrayList<Enchantress> enchantresses = new ArrayList<>();
    Princess princess = new Princess("Alice", Emotion.AMUSED);
    Witch witch = new Witch("Delayla", Emotion.STUNNED);

    public void preparing(){
        String[] names = {"Sidney", "Katrin", "Jenny", "Hanna", "Mary", "Judy", "Shain"};
        Emotion[] emotions = {Emotion.AMAZED, Emotion.ASTONISHED, Emotion.DELIGHTED, Emotion.ECSTATIC, Emotion.OVERJOYED, Emotion.SHOCKED, Emotion.THRILLED};
        for (int i = 0; i < 7; i++){
            Enchantress e = new Enchantress(names[i], emotions[i]);
            enchantresses.add(e);
        }
    }

    public void part1(){
        Toys[] toys = {Toys.TOY_PRINCESS, Toys.DOLL, Toys.DRESS, Toys.BOOK_OF_FAIRY_TALES, Toys.TOY_CASTLE, Toys.BOX_OF_SWEETS, Toys.TEDDY_BEAR};
        Qualities[] qualities = {Qualities.BEAUTY, Qualities.COURAGE, Qualities.DETERMINATION, Qualities.ENDURANCE, Qualities.INTELLIGENCE, Qualities.KINDNESS, Qualities.POLITENESS};
        System.out.println(princess);
        for (int i = 0; i < 7; i++){

            enchantresses.get(i).beInvited();
            Gift gift = new Gift(toys[i], qualities[i]);

            try{
                enchantresses.get(i).makeGift(gift, princess);
            } catch (GiftOverflow go){
                System.out.println(go.getMessage());
                System.out.println("\n");
                break;
            } catch (DoNotLikeGift dnlg){
                System.out.println(dnlg.getMessage());
            }
        }
    }

    public void part2(){
        Material[] materials = {Material.CRYSTAL, Material.GOLDEN, Material.PORCELAIN, Material.SILVER};
        PreciousStones[] stones = {PreciousStones.BRILLIANTS, PreciousStones.EMERALDS, PreciousStones.RUBIES, PreciousStones.SAPPIRES};
        DinnerDishes[] dinnerDishes = {DinnerDishes.PIGS_IN_BLANKETS, DinnerDishes.CORONATION_CHICKEN, DinnerDishes.ETON_MESS,
                DinnerDishes.YORKSHIRE_PUDDING, DinnerDishes.COLD_ROAST_BEEF_AND_HORSERADISH_ROLLS, DinnerDishes.SALMON_AND_CUCUMBER_SANDWICHES, DinnerDishes.STRAWBERRIES_AND_CREAM};
        for (int i = 0; i < 7; i++){
            enchantresses.get(i).getSetOfDishes(new SetOfDishes(new Plate(materials[i%4], stones[i%4]),
                    new Fork(materials[i%4]), new Spoon(materials[i%4]), new Knife(materials[i%4]), new Glass(materials[i%4])));
            enchantresses.get(i).getDinner(dinnerDishes[i]);
            System.out.println(enchantresses.get(i));
            System.out.println("\n");
        }
        System.out.println(witch + "\n");
    }

    public void part3(){
        System.out.println(princess + " got this gifts:");
        princess.showGifts();
    }

    @Override
    public String toString() {
        return "This is a story text:\n";
    }
}