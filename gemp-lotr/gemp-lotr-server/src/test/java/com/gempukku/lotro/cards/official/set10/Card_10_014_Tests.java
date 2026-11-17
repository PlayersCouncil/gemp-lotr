package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("borne", "10_14");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: your Wizard
					put("gimli", "5_7");        // Gimli, Skilled Defender: a companion in hand
					put("legolas", "1_50");     // Legolas, Greenleaf: a second companion in hand
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion in the dead pile
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the companion in the discard pile
					put("ent", "6_27");         // Ent Avenger: a [gandalf] companion who is no Wizard
					// Saruman, The White: a [gandalf] Wizard ally, home 3H (Q-0312: the one kind of Wizard who is no
					// companion); his abilities are used only on purpose
					put("saruman", "32_24");
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; his ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] ally; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRaceTextScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("borne", "10_14");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: your Wizard
					put("gimli", "5_7");        // Gimli, Skilled Defender: the companion in hand
					put("boromir", "1_96");     // Boromir, Lord of Gondor: the companion in the discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				// Race Text 94_30 in player 1's seat: "You may not play or take into hand cards from your discard
				// pile or draw deck (except when reconciling)"
				"94_30", null
		);
	}

	@Test
	public void BorneFarAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Borne Far Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot your Wizard to exchange a companion in hand with a companion in your dead pile or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("borne");

		assertEquals("Borne Far Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExchangesACompanionInHandWithACompanionInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ent = scn.GetFreepsCard("ent");
		var barliman = scn.GetFreepsCard("barliman");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gandalf);
		// Two companions and an ally in hand; two companions and an ally in the dead pile, so that both of this
		// pile's choices have something to be told from; a companion in the discard pile, so the pile is asked.
		scn.MoveCardsToHand(borne, gimli, legolas, barliman);
		scn.MoveCardsToDeadPile(aragorn, ent, bounder);
		scn.MoveCardsToDiscard(boromir);
		assertInZone(Zone.HAND, borne, gimli, legolas, barliman);
		assertInZone(Zone.DEAD, aragorn, ent, bounder);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		// A companion lies in each pile, so the player is asked which pile to exchange with.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("dead pile");
		// The companion in hand to give: not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// The companion in the dead pile to take: not the ally, nor the companion in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, ent));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.HAND, aragorn, legolas, barliman);
		assertInZone(Zone.DEAD, gimli, ent, bounder);
		assertInZone(Zone.DISCARD, boromir);
		assertInDiscard(borne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExchangesACompanionInHandWithACompanionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(borne, gimli, legolas);
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToDiscard(boromir);
		assertInZone(Zone.HAND, borne, gimli, legolas);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("discard");
		// Boromir is the only companion in the discard pile; which companion in hand goes there is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.HAND, boromir);
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.HAND, legolas);
		assertInZone(Zone.DEAD, aragorn);
		assertInDiscard(borne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void OnlyCompanionsAreExchangedAndAWizardAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var saruman = scn.GetFreepsCard("saruman");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		// The only Wizard is an ally.
		scn.MoveAlliesToTable(saruman);
		// Two companions and an ally in hand; two companions and an ally in the discard pile; the dead pile empty.
		scn.MoveCardsToHand(borne, gimli, legolas, barliman);
		scn.MoveCardsToDiscard(aragorn, boromir, bounder);
		assertInZone(Zone.SUPPORT, saruman);
		assertInZone(Zone.HAND, borne, gimli, legolas, barliman);
		assertInZone(Zone.DISCARD, aragorn, boromir, bounder);
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		// No companion in the dead pile, so the discard pile is the one exchanged with, unasked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.HAND, aragorn, gimli, barliman);
		assertInZone(Zone.DISCARD, legolas, boromir, bounder, borne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionInEitherPileItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(borne, gimli);
		// An ally in the discard pile, and nothing in the dead pile.
		scn.MoveCardsToDiscard(bounder);
		assertInZone(Zone.HAND, borne, gimli);
		assertInZone(Zone.DISCARD, bounder);
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.StartGame();

		// The Wizard is spotted, so the event is played; there is no companion to take in exchange.
		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		// Neither pile can be exchanged with: nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, bounder, borne);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionInHandItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf);
		// An ally in hand; a companion in each pile.
		scn.MoveCardsToHand(borne, barliman);
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToDiscard(boromir);
		assertInZone(Zone.HAND, borne, barliman);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		// No companion in hand to give in exchange: nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.HAND, barliman);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, boromir, borne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAWizardToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borne = scn.GetFreepsCard("borne");
		var ent = scn.GetFreepsCard("ent");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		// A [gandalf] companion who is no Wizard, and companions to exchange.
		scn.MoveCompanionsToTable(ent);
		scn.MoveCardsToHand(borne, gimli);
		scn.MoveCardsToDiscard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, ent);
		assertInZone(Zone.HAND, borne, gimli);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(borne));
	}
	@Test
	public void UnderARaceTextThatBarsTakingCardsFromTheDiscardPileTheExchangeStillHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRaceTextScenario();

		var borne = scn.GetFreepsCard("borne");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(borne, gimli);
		scn.MoveCardsToDiscard(boromir);
		assertInZone(Zone.HAND, borne, gimli);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();

		// Ketura (Q-0217: B): an exchange is no take into hand, so the bar does not stop it.
		assertTrue(scn.FreepsPlayAvailable(borne));
		scn.FreepsPlayCard(borne);

		assertInZone(Zone.HAND, boromir);
		assertInZone(Zone.DISCARD, gimli, borne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
