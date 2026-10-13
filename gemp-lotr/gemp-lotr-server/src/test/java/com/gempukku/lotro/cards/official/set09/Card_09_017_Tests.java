package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "9_17");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8
					put("rangersword", "1_112");  // Ranger's Sword: a hand weapon, strength +2; "Bearer must be Aragorn."
					put("boromir", "1_96");       // Boromir, Lord of Gondor: a [gondor] Man who is not Aragorn
					// Sword of Gondor: a hand weapon, "Bearer must be a [gondor] Man"; its damage +1 is for a skirmish
					put("gondorsword", "4_134");
					put("knight", "5_35");        // Gondorian Knight: a [gondor] Man with no text, who bears nothing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnifeoftheGaladhrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Knife of the Galadhrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [gondor] Man. If bearer is Aragorn, he may bear this hand weapon in addition to 1 other hand weapon.<br><b>Skirmish:</b> Discard this weapon to wound a minion bearer is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Knife of the Galadhrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AragornMayBearItBesideOneOtherHandWeaponAndAnotherGondorManMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetFreepsCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var boromir = scn.GetFreepsCard("boromir");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(boromir, gondorsword);
		scn.MoveCardsToHand(knife);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		// 8 + 2 for Ranger's Sword
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(knife));
		scn.FreepsPlayCard(knife);

		// All three are [gondor] Men.  Aragorn has a hand weapon and may bear the Knife beside it; the Knight has
		// none; Boromir has one and is not Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(knife, aragorn);
		assertAttachedTo(rangersword, aragorn);
		// 8 + 2 for Ranger's Sword + 1 for the Knife
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
