package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_032_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elendil", "59_32");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("defender", "17_93");   // Aragorn, Defender of Rohan: the Aragorn who is [rohan]; his text needs the Ring-bearer assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, a Man, cost 1; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a Man who is [rohan]; valiant and nothing else
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] artifact, cost 2, bearer must be a [gondor] Man; a response to a wound
					put("forged", "17_31");     // Narsil, Forged by Telchar: [gondor] artifact, cost 3, bearer must be Elendil or Isildur
					put("barahir", "6_55");     // Ring of Barahir: [gondor] artifact, cost 0, bearer must be Aragorn; an optional trigger at the start of a turn
					put("shards", "3_44");      // The Shards of Narsil: [gondor] artifact that plays to the support area; fellowship abilities used only on purpose
					put("armor", "1_92");       // Armor: [gondor] possession, cost 1, bearer must be a Man; caps the wounds of a skirmish
					put("aiglos", "9_12");      // Aiglos: [elven] artifact, bearer must be an Elf; a regroup ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElendilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Elendil, The Tall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 5
		 * Resistance: 6
		 * Game Text: To play, spot 2 [gondor] Men.
		 * 	<b>Fellowship:</b> Play an artifact on Elendil to make the move limit +1 for this turn.
		 * 	<b>Regroup:</b> Discard a [gondor] artifact borne by a companion to liberate a site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elendil");

		assertEquals("Elendil", card.getBlueprint().getTitle());
		assertEquals("The Tall", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilTwoGondorMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		// One [gondor] Man, a [gondor] companion who is no Man, and a Man who is not [gondor].
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToHand(elendil, boromir);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elendil));

		scn.FreepsPlayCard(boromir);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elendil));

		scn.FreepsPlayCard(elendil);
		// 3 for Boromir + 5 for Elendil.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elendil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorManWhoIsAnAllyIsOneOfTheTwoMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(elendil, merchant);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elendil));

		scn.FreepsPlayCard(merchant);
		assertInZone(Zone.SUPPORT, merchant);
		assertTrue(scn.FreepsPlayAvailable(elendil));

		scn.FreepsPlayCard(elendil);
		// 1 for the Merchant + 5 for Elendil.
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elendil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysAnArtifactFromHandOnElendilToMakeTheMoveLimitPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var forged = scn.GetFreepsCard("forged");
		var barahir = scn.GetFreepsCard("barahir");
		var shards = scn.GetFreepsCard("shards");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(elendil, aragorn);
		scn.MoveCardsToHand(narsil, forged, barahir, shards, armor);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(elendil));

		scn.FreepsUseCardAction(elendil);

		// Either Narsil; not an artifact that is Aragorn's to bear, nor one that plays to the support area,
		// nor a possession Elendil could bear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(narsil, forged));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barahir, shards, armor));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(narsil);

		// Aragorn could bear this Narsil too, and is not asked about: it is played on Elendil.
		assertAttachedTo(narsil, elendil);
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetMoveLimit());
		assertInZone(Zone.HAND, forged, barahir, shards, armor);

		// Elendil now bears a Narsil and a hand weapon, so the other Narsil cannot be played on him: with no
		// artifact left that can be, the ability is not offered.
		assertFalse(scn.FreepsActionAvailable(elendil));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithoutAnArtifactInHandThatElendilCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var forged = scn.GetFreepsCard("forged");
		var barahir = scn.GetFreepsCard("barahir");
		var shards = scn.GetFreepsCard("shards");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(elendil, aragorn);
		scn.MoveCardsToHand(barahir, shards, armor);

		scn.StartGame();

		// Each of the three can be played, but not as an artifact on Elendil.
		assertTrue(scn.FreepsPlayAvailable(barahir));
		assertTrue(scn.FreepsPlayAvailable(shards));
		assertTrue(scn.FreepsPlayAvailable(armor));
		assertFalse(scn.FreepsActionAvailable(elendil));

		scn.MoveCardsToHand(forged);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(elendil));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheMoveLimitIsPlus1ForTheWholeTurnAndNotTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var forged = scn.GetFreepsCard("forged");
		scn.MoveCompanionsToTable(elendil);
		scn.MoveCardsToHand(forged);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		// The one artifact in hand is played without a choice.
		scn.FreepsUseCardAction(elendil);
		assertAttachedTo(forged, elendil);
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// Two moves made, and in the regroup phase a third is still offered.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());

		// The next turn (the other player's): the limit is 2 again.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertAttachedTo(forged, elendil);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RegroupActionDiscardsAGondorArtifactBorneByACompanionToLiberateTheHighestSiteTheOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		// A companion of another culture as the second bearer: the Aragorn who is [rohan].
		var aragorn = scn.GetFreepsCard("defender");
		var legolas = scn.GetFreepsCard("legolas");
		var narsil = scn.GetFreepsCard("narsil");
		var barahir = scn.GetFreepsCard("barahir");
		var shards = scn.GetFreepsCard("shards");
		var armor = scn.GetFreepsCard("armor");
		var aiglos = scn.GetFreepsCard("aiglos");

		scn.StartGame();
		// Both players are at site 3, so sites 1 and 2 can be controlled.
		scn.SkipToSite(3);

		scn.MoveCompanionsToTable(elendil, aragorn, legolas);
		scn.AttachCardsTo(elendil, narsil);
		scn.AttachCardsTo(aragorn, barahir, armor);
		scn.AttachCardsTo(legolas, aiglos);
		scn.MoveCardsToSupportArea(shards);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));

		// A regroup action: not offered in the fellowship phase.
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elendil));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(elendil));

		scn.FreepsUseCardAction(elendil);

		// A [gondor] artifact on Elendil or on another companion; not an [elven] artifact on a companion,
		// a [gondor] possession on a companion, or a [gondor] artifact in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(narsil, barahir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aiglos, armor, shards));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(barahir);

		assertInZone(Zone.DISCARD, barahir);
		assertAttachedTo(narsil, elendil);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(aiglos, legolas);
		assertInZone(Zone.SUPPORT, shards);
		// One site, and the highest-numbered first.
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithoutAGondorArtifactBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var barahir = scn.GetFreepsCard("barahir");
		var shards = scn.GetFreepsCard("shards");
		var armor = scn.GetFreepsCard("armor");
		var aiglos = scn.GetFreepsCard("aiglos");
		scn.MoveCompanionsToTable(elendil, aragorn, legolas);
		scn.MoveAlliesToTable(merchant);
		// A [gondor] artifact borne by an ally, an [elven] artifact and a [gondor] possession borne by
		// companions, and a [gondor] artifact that nobody bears.
		scn.AttachCardsTo(merchant, narsil);
		scn.AttachCardsTo(legolas, aiglos);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToSupportArea(shards);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elendil));

		scn.AttachCardsTo(aragorn, barahir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(elendil));

		// The one card that can pay is discarded without a choice.  Nobody controls a site, so nothing is
		// liberated: the discard is a cost, and is paid for nothing.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.FreepsUseCardAction(elendil);

		assertInZone(Zone.DISCARD, barahir);
		assertAttachedTo(narsil, merchant);
		assertAttachedTo(aiglos, legolas);
		assertAttachedTo(armor, aragorn);
		assertInZone(Zone.SUPPORT, shards);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
