package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_85");
					put("card2", "15_85");
					put("hillman1", "15_82");   // Grousing Hillman: a [men] Man, vitality 2; his text needs a controlled site
					put("hillman2", "15_82");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound companion
					// Dwarven Armor: a Free Peoples possession that changes no number, put on the Men by the rig
					put("armor1", "1_8");
					put("armor2", "1_8");
					put("armor3", "1_8");
					put("armor4", "1_8");
					put("armor5", "1_8");
					put("armor6", "1_8");
					put("armor7", "1_8");
					put("armor8", "1_8");
					put("poison1", "8_28");     // Spider Poison: a Shadow card that a companion bears
					put("poison2", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LyingCounselStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Lying Counsel
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your [men] Man and spot an unbound companion bearing 3 or more cards to return each Free Peoples card that companion bears to its owner's hand. The Free Peoples player may add a burden to discard those cards instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Lying Counsel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void LyingCounselTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void AHinderedFreePeoplesCardThatCompanionBearsIsReturnedOrDiscardedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var card2 = scn.GetShadowCard("card2");
		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");
		var poison1 = scn.GetShadowCard("poison1");
		var poison2 = scn.GetShadowCard("poison2");
		scn.MoveCardsToHand(card, card2);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		var armor3 = scn.GetFreepsCard("armor3");
		var hindered1 = scn.GetFreepsCard("armor4");
		var armor5 = scn.GetFreepsCard("armor5");
		var armor6 = scn.GetFreepsCard("armor6");
		var armor7 = scn.GetFreepsCard("armor7");
		var hindered2 = scn.GetFreepsCard("armor8");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Each bears three cards that are not hindered, a hindered Free Peoples card and a Shadow card.
		scn.AttachCardsTo(aragorn, armor1, armor2, armor3, hindered1);
		scn.AttachCardsTo(boromir, armor5, armor6, armor7, hindered2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, poison1);
		scn.AttachCardsTo(boromir, poison2);
		// A hindered card has no type or culture, and is a Free Peoples card still (CR 5.0 "hinder"; Ketura on
		// Q-0098: "each Free Peoples card" reaches it).
		scn.HinderCard(hindered1, hindered2);
		scn.MoveMinionsToTable(hillman1, hillman2);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(hindered1));
		assertTrue(scn.IsHindered(hindered2));
		assertEquals(0, scn.GetFreepsHandCount());
		int burdens = scn.GetBurdens();

		scn.ShadowPlayCard(card);
		scn.ShadowChooseCard(hillman1);
		scn.ShadowChooseCard(aragorn);
		// The Free Peoples player does not add a burden: the cards go back to hand, the hindered one with them.
		scn.FreepsChooseNo();

		assertEquals(burdens, scn.GetBurdens());
		assertInZone(Zone.HAND, armor1, armor2, armor3, hindered1);
		assertFalse(scn.IsHindered(hindered1));
		assertAttachedTo(poison1, aragorn);
		assertAttachedTo(armor5, boromir);

		// The second copy: the other Hillman exerts, Boromir is the one companion left bearing 3 cards, and the
		// Free Peoples player adds a burden: the cards are discarded, the hindered one with them.
		scn.FreepsPass();
		scn.ShadowPlayCard(card2);
		scn.FreepsChooseYes();

		assertEquals(burdens + 1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, armor5, armor6, armor7, hindered2);
		assertAttachedTo(poison2, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
