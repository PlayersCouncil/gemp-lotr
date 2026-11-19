package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("answers", "12_167");

					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl; her text reaches bearers of [wraith] conditions only
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, fierce; the rest needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl; nothing below 3 burdens

					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion to bear the [shire] cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: bears The Weight of a Legacy
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1
					put("tale", "1_317");       // There and Back Again: [shire] condition borne by a Hobbit companion; a maneuver ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, a Free Peoples item that is no condition; no text but its bearer
					put("defenses", "1_274");   // Sauron's Defenses: [sauron] condition in the Shadow support area; acts only beside 3 dead companions
					put("weight", "1_282");     // The Weight of a Legacy: [sauron] condition borne by Aragorn, strength -1; no text but its cost and bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinasMorgulAnswersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Minas Morgul Answers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Toil 2</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot a Nazgûl to discard a Free Peoples condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("answers");

		assertEquals("Minas Morgul Answers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsANazgulToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var lemenya = scn.GetShadowCard("lemenya");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var tale = scn.GetFreepsCard("tale");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.AttachCardsTo(sam, tale, sword);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(defenses);
		scn.AttachCardsTo(aragorn, weight);
		assertInZone(Zone.SUPPORT, lord, defenses);
		assertAttachedTo(tale, sam);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(answers));
		scn.ShadowPlayCard(answers);
		// Toil: the pool pays in full, so Lemenya is offered to exert, and nobody is.
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(lemenya));

		// A Free Peoples condition in the support area or borne; not a Free Peoples possession, nor a Shadow
		// condition in the support area or on a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, tale));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, defenses, weight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tale);

		assertInDiscard(tale);
		assertInZone(Zone.SUPPORT, lord, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInDiscard(answers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedBesideAWraithOrcAloneAndCanOnceANazgulOfAnotherCultureIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var creeper = scn.GetShadowCard("creeper");
		var khamul = scn.GetShadowCard("khamul");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(answers);
		scn.MoveMinionsToTable(creeper);
		scn.SetTwilight(10);

		// A [wraith] minion who is no Nazgûl, a pool that pays in full and a condition to discard: not playable.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(answers));

		// Khamûl, a [raider] Nazgûl, put down beside him: playable.
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(answers));
		scn.ShadowPlayCard(answers);
		// Toil: the Creeper is the one [wraith] character; nobody is exerted.
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(creeper));

		// The one Free Peoples condition is discarded with nothing asked.
		assertInDiscard(lord);
		assertInDiscard(answers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var answers = scn.GetShadowCard("answers");
		var lemenya = scn.GetShadowCard("lemenya");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(defenses);
		scn.AttachCardsTo(aragorn, weight);
		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		scn.SetTwilight(10);

		scn.FreepsPass();
		// The condition to discard is no requirement: the event is played with none to choose.
		assertTrue(scn.ShadowPlayAvailable(answers));
		scn.ShadowPlayCard(answers);
		scn.ShadowDeclineChoosing();

		assertInDiscard(answers);
		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
