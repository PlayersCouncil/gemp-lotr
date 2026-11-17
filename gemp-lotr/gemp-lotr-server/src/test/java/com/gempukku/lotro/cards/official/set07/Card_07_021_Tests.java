package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "7_21");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Aragorn; a maneuver ability only
					put("rohanAragorn", "17_93"); // Aragorn, Defender of Rohan: the [rohan] Aragorn; his text needs the Ring-bearer assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is no Aragorn; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion who is no Elf; a strength bonus only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability only
					put("orophin", "1_56");     // Orophin, Lórien Bowman: an Elf ally; an archery ability only
					put("staff", "2_22");       // Gandalf's Staff: an artifact Gandalf bears, vitality +1; a cost bonus and a skirmish ability
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ally, vitality 1; a maneuver ability only
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an artifact a [gondor] Man bears, vitality +1; a response to a wound only
					put("sword", "1_112");      // Ranger's Sword: a POSSESSION Aragorn bears, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Elrond, Elven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot Aragorn or an Elf.
		 * At the start of each turn, you may exert Elrond to heal a character bearing an artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Elven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAragornIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(boromir, gandalf);
		scn.MoveCardsToHand(elrond, aragorn);

		scn.StartGame();

		// A [gondor] Man who is no Aragorn, and a companion who is no Elf: neither is spotted.
		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornOfAnotherCultureIsSpottedToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var rohanAragorn = scn.GetFreepsCard("rohanAragorn");
		scn.MoveCompanionsToTable(rohanAragorn);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();

		// The [rohan] Aragorn, and no Elf.
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(elrond, orophin);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elrond));

		// An Elf who is an ally: the text asks for an Elf, not for an Elf companion.
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfCompanionIsSpottedToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheTurnExertsElrondToHealACharacterBearingAnArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elrond, gandalf, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(gandalf, staff);
		scn.AttachCardsTo(merchant, narsil);
		scn.AttachCardsTo(aragorn, sword);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(gandalf, staff.getAttachedTo());
		assertEquals(merchant, narsil.getAttachedTo());
		assertEquals(aragorn, sword.getAttachedTo());

		// The first turn starts here.
		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		// A companion and an ally, each bearing an artifact. Not a companion bearing a possession, nor the
		// Ring-bearer, who bears The One Ring and nothing else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheStartOfTurnOfferChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(elrond, gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.AddWoundsToChar(gandalf, 1);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(gandalf, staff.getAttachedTo());

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExhaustedElrondIsNotOfferedTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(elrond, gandalf);
		scn.AttachCardsTo(gandalf, staff);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(elrond, 3);
		scn.AddWoundsToChar(gandalf, 1);

		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(gandalf, staff.getAttachedTo());

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedCharacterBearingAnArtifactElrondExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(elrond, gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.AttachCardsTo(aragorn, sword);
		// The one wounded character bears a possession, and the one artifact's bearer is unwounded.
		scn.AddWoundsToChar(aragorn, 1);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(gandalf, staff.getAttachedTo());
		assertEquals(aragorn, sword.getAttachedTo());

		scn.StartGame();
		// The exertion is a cost, so the offer is made though the heal can do nothing.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfItsOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(elrond, gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.AddWoundsToChar(gandalf, 3);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(gandalf, staff.getAttachedTo());

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// The other player's turn: this player's Free Peoples cards are not active.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
