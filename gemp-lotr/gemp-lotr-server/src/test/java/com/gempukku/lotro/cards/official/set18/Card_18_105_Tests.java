package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "18_105");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength +1 for each race in the fellowship; nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only beside 3 Ents
					put("eomer", "4_267");      // Éomer, Third Marshal of Riddermark: cost -1 beside a [rohan] Man; stronger only in a skirmish
					put("spear", "4_268");      // Éomer's Spear: a card with Éomer in its title; damage only for Éomer mounted
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5; a skirmish ability only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; valiant is its whole text
					put("eowyn", "5_122");      // Éowyn, Daughter of Éomund: [rohan] Man companion, strength 6; a maneuver ability only
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a regroup ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, a knight; Knight is its whole text
					put("knightsmount", "8_40");// Knight's Mount: [gondor] mount on a knight, strength +1; a trigger on a skirmish won
					put("riderspear", "4_288"); // Rider's Spear: [rohan] hand weapon, strength +2; no mount
					// Rider's Mount: [rohan] mount, no bonus; a start-of-skirmish trigger only
					put("mount1", "4_287");
					put("mount2", "4_287");
					put("mount3", "4_287");
					// Rohirrim Mount: [rohan] mount, no bonus; defender +1 at a plains site (none here) and a
					// start-of-skirmish trigger
					put("rmount", "11_156");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Théoden, Ednew
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot Gandalf, Théoden is resistance +2.<br>While you can spot Éomer, Théoden is <b>damage +1</b>.<br>While you can spot 3 mounted [rohan] companions, Théoden is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Ednew", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileGandalfCanBeSpottedTheodenIsResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theoden, avenger, guard);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		// A [gandalf] companion who is no Gandalf: nothing.
		assertEquals(6, scn.GetResistance(theoden));
		assertEquals(6, scn.GetResistance(guard));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		assertEquals(8, scn.GetResistance(theoden));
		// Théoden alone, and for Gandalf only that.
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileEomerCanBeSpottedTheodenIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(theoden, rider);
		scn.AttachCardsTo(theoden, spear);
		scn.MoveCardsToHand(eomer);
		assertAttachedTo(spear, theoden);

		scn.StartGame();

		// A [rohan] companion of another title, and a card with Éomer in its title: nothing.
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));

		scn.FreepsPlayCard(eomer);
		assertInZone(Zone.FREE_CHARACTERS, eomer);

		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		// Théoden alone, and for Éomer only that.
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(theoden));
		// 7 + 2 for the Spear
		assertEquals(9, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheThirdMountedRohanCompanionMakesHimStrengthPlus2AndHeCountsHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var knightsmount = scn.GetFreepsCard("knightsmount");
		var riderspear = scn.GetFreepsCard("riderspear");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var mount3 = scn.GetFreepsCard("mount3");
		var rmount = scn.GetFreepsCard("rmount");
		scn.MoveCompanionsToTable(theoden, rider, guard, knight);
		scn.MoveAlliesToTable(herdsman);
		// Mounted: the Rider of Rohan ([rohan] companion), the Herdsman ([rohan] ally) and the Knight ([gondor]
		// companion). The Guard bears a possession that is no mount.
		scn.AttachCardsTo(rider, rmount);
		scn.AttachCardsTo(herdsman, mount1);
		scn.AttachCardsTo(knight, knightsmount);
		scn.AttachCardsTo(guard, riderspear);
		scn.MoveCardsToHand(mount2, mount3);
		assertAttachedTo(rmount, rider);
		assertAttachedTo(mount1, herdsman);
		assertAttachedTo(knightsmount, knight);
		assertAttachedTo(riderspear, guard);

		scn.StartGame();

		// One mounted [rohan] companion.
		assertEquals(7, scn.GetStrength(theoden));

		scn.FreepsPlayCard(mount2);
		scn.FreepsChooseCard(guard);
		assertAttachedTo(mount2, guard);
		// Two: still 7.  The Guard is 6 + 2 for the Spear.
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(guard));

		// Théoden is now the only [rohan] Man with no mount: it goes on him unasked.
		scn.FreepsPlayCard(mount3);
		assertAttachedTo(mount3, theoden);
		// Théoden mounted is the third: 7 + 2.  The Guard, who meets the same count, is unchanged.
		assertEquals(9, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThreeOtherMountedRohanCompanionsMakeHimStrengthPlus2ThoughHeIsNotMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var rmount = scn.GetFreepsCard("rmount");
		scn.MoveCompanionsToTable(theoden, rider, guard, eowyn);
		scn.AttachCardsTo(rider, rmount);
		scn.AttachCardsTo(guard, mount1);
		scn.AttachCardsTo(eowyn, mount2);
		assertAttachedTo(rmount, rider);
		assertAttachedTo(mount1, guard);
		assertAttachedTo(mount2, eowyn);

		scn.StartGame();

		assertEquals(9, scn.GetStrength(theoden));
		assertEquals(6, scn.GetStrength(guard));
	}
}
