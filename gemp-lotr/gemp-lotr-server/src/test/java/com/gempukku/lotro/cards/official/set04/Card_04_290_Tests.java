package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_290_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "4_290");

					// Villagers to place: Guma, Plains Farmer and Hlafwine, Village Farmhand, [rohan] allies.
					put("guma", "4_277");
					put("hlafwine", "4_281");
					// Aldor, Soldier of Edoras: a [rohan] ally who is no villager; an archery ability used only on purpose.
					put("aldor", "4_262");

					// Royal Guard: [rohan] ally, vitality 3, no villager; its "To play" is passed over by the cheat and its
					// skirmish ability is used only on purpose.
					put("royalguard", "102_54");
					// Léowyn: [rohan] ally and villager, vitality 2; a fellowship ability that needs initiative and two
					// cards discarded, used only on purpose.
					put("leowyn", "7_239");
					put("hguard", "5_83");      // Household Guard: a [rohan] companion who is no ally; Valiant only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A wounded [rohan] ally and a wounded [shire] ally, with a villager in hand to pay with.
	protected VirtualTableScenario GetTwoWoundedAlliesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("supplies", "4_290");
					put("guma", "4_277");
					put("hlafwine", "4_281");
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuppliesoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Supplies of the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Fellowship:</b> Add (2) and place a villager from hand beneath your draw deck to heal a [rohan] ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("supplies");

		assertEquals("Supplies of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "to heal a [rohan] ally": the definition healed any ally.
	@Test
	public void OnlyARohanAllyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWoundedAlliesScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(supplies);
		scn.MoveAlliesToTable(guma, bounder);
		scn.MoveCardsToHand(hlafwine);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInHand(hlafwine);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);

		// Hlafwine is the only villager in hand and Guma the only [rohan] ally: nothing is asked.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DECK, hlafwine);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityAddsTwoAndPlacesAVillagerBeneathTheDrawDeckToHealARohanAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var aldor = scn.GetFreepsCard("aldor");
		var royalguard = scn.GetFreepsCard("royalguard");
		var leowyn = scn.GetFreepsCard("leowyn");
		var hguard = scn.GetFreepsCard("hguard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToSupportArea(supplies);
		scn.MoveCardsToHand(guma, hlafwine, aldor);
		scn.MoveCompanionsToTable(hguard);
		scn.MoveAlliesToTable(royalguard, leowyn, barliman);
		scn.AddWoundsToChar(royalguard, 2);
		scn.AddWoundsToChar(leowyn, 1);
		scn.AddWoundsToChar(hguard, 1);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(royalguard));
		assertEquals(1, scn.GetWoundsOn(leowyn));
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInHand(guma, hlafwine, aldor);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);

		// Either villager in hand may pay; Aldor is a [rohan] ally but no villager.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guma, hlafwine));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertEquals(2, scn.GetTwilight());
		assertSame(guma, scn.GetFreepsBottomOfDeck());
		assertInHand(hlafwine, aldor);

		// The wounded [rohan] allies, a villager and one who is none; not the [rohan] companion, nor the [gandalf] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(royalguard, leowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hguard, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(royalguard);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(royalguard));
		assertEquals(1, scn.GetWoundsOn(leowyn));
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoVillagerInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var aldor = scn.GetFreepsCard("aldor");
		var royalguard = scn.GetFreepsCard("royalguard");
		var leowyn = scn.GetFreepsCard("leowyn");
		scn.MoveCardsToSupportArea(supplies);
		scn.MoveCardsToHand(aldor);
		// Léowyn is a villager, but in play and not in hand.
		scn.MoveAlliesToTable(royalguard, leowyn);
		scn.AddWoundsToChar(royalguard, 1);
		scn.AddWoundsToChar(leowyn, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(royalguard));
		assertEquals(1, scn.GetWoundsOn(leowyn));
		assertInHand(aldor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(supplies));
	}

	@Test
	public void WithNoWoundedRohanAllyItIsOfferedAndBothCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var supplies = scn.GetFreepsCard("supplies");
		var guma = scn.GetFreepsCard("guma");
		var royalguard = scn.GetFreepsCard("royalguard");
		var hguard = scn.GetFreepsCard("hguard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToSupportArea(supplies);
		scn.MoveCardsToHand(guma);
		scn.MoveCompanionsToTable(hguard);
		// The [rohan] ally is unwounded; the wounded are a [rohan] companion and a [gandalf] ally.
		scn.MoveAlliesToTable(royalguard, barliman);
		scn.AddWoundsToChar(hguard, 1);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(royalguard));
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInHand(guma);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(supplies));
		scn.FreepsUseCardAction(supplies);

		assertEquals(2, scn.GetTwilight());
		assertSame(guma, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(royalguard));
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
