package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marshal", "10_53");
					put("marshal2", "10_53");   // a second copy, in hand: a [wraith] card that is no minion
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, vitality 2; her text reads only companions bearing [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3; his text needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] Orc, no Nazgûl, vitality 2; his text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, cost 2, site 4; his text needs 2 wounds on the Ring-bearer
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: [wraith] Nazgûl, cost 5; a skirmish ability used only on purpose
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, cost 2; a skirmish ability used only on purpose
					// Old Toby: Free Peoples cards for the Free Peoples hand, which decides initiative
					put("fp1", "1_305");
					put("fp2", "1_305");
					put("fp3", "1_305");
					put("fp4", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackMarshalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Black Marshal
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Shadow:</b> Exert a Nazgûl and play a [wraith] minion to add (1) (or (2) if you have initiative).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marshal");

		assertEquals("Black Marshal", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityExertsANazgulAndPlaysAWraithMinionToAddOneWithoutInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var marshal2 = scn.GetShadowCard("marshal2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(marshal);
		scn.MoveCardsToHand(hound, cantea, soldier, marshal2);
		scn.MoveCardsToHand(scn.GetFreepsCard("fp1"), scn.GetFreepsCard("fp2"), scn.GetFreepsCard("fp3"), scn.GetFreepsCard("fp4"));
		assertInHand(hound, cantea, soldier, marshal2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, creeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertInHand(hound, cantea, soldier, marshal2);

		assertTrue(scn.ShadowActionAvailable(marshal));
		scn.ShadowUseCardAction(marshal);

		// A Nazgûl of either culture; not a [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(creeper));

		// A [wraith] Orc or a [wraith] Nazgûl; not a [sauron] minion, nor a [wraith] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, cantea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, marshal2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertInHand(cantea, soldier, marshal2);
		// 2 to play the Hound + 2 roaming (site 4 at site 2), then (1) added
		assertEquals(pool - 2 - 2 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithInitiativeTheShadowAbilityAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var marshal2 = scn.GetShadowCard("marshal2");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(marshal);
		scn.MoveCardsToHand(hound, cantea, soldier, marshal2);
		scn.MoveCardsToHand(scn.GetFreepsCard("fp1"), scn.GetFreepsCard("fp2"), scn.GetFreepsCard("fp3"));
		assertInHand(hound, cantea, soldier, marshal2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Three cards in the Free Peoples hand: the Shadow has initiative, though the Shadow hand holds four.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(lemenya));

		assertTrue(scn.ShadowActionAvailable(marshal));
		scn.ShadowUseCardAction(marshal);
		// Lemenya is the one Nazgûl, and exerts with nothing asked.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		scn.ShadowChooseCard(hound);

		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		// 2 to play the Hound + 2 roaming (site 4 at site 2), then (2) added
		assertEquals(pool - 2 - 2 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(marshal);
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		// Vitality 2: with 1 wound Lemenya cannot exert. The Creeper beside her could, were he a Nazgûl.
		scn.MoveMinionsToTable(lemenya, creeper);
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertInHand(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshal));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertInHand(hound);
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoWraithMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var marshal2 = scn.GetShadowCard("marshal2");
		var lemenya = scn.GetShadowCard("lemenya");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(marshal);
		// A minion of another culture and a [wraith] card that is no minion, both of which could be played.
		scn.MoveCardsToHand(soldier, marshal2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(soldier));
		assertTrue(scn.ShadowPlayAvailable(marshal2));
		assertFalse(scn.ShadowActionAvailable(marshal));
		assertEquals(0, scn.GetWoundsOn(lemenya));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForTheWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(marshal);
		scn.MoveCardsToHand(hound);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Morgul Hound: 2 to play + 2 roaming (site 4 at site 2), one short
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(marshal));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(marshal));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(marshal);
		scn.MoveCardsToHand(hound);

		assertInHand(hound);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInHand(hound);
		// The pool still holds more than the Hound's 2 + 2 roaming.
		assertTrue(scn.GetTwilight() >= 4);
		assertFalse(scn.ShadowActionAvailable(marshal));
	}
}
