package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flung", "8_71");
					// Web: [gollum] support possession.  "Regroup: If there are fewer than 3 cards stacked here, spot
					// your Orc or [gollum] minion to stack that minion here.  Shadow: Play a [gollum] minion stacked
					// here as if played from hand."  A [wraith] Orc can be stacked on it by its own text.
					put("web", "8_30");
					// Morgul Lackey: [wraith] Orc, cost 2; its skirmish ability is never used here
					put("lackey1", "7_193");
					put("lackey2", "7_193");
					put("onweb", "7_193");
					put("onflung", "7_193");
					put("inhand", "7_193");
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: a [wraith] Nazgul; fierce; a skirmish ability used only on purpose
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, cost 2, site 4; its text needs 2 wounds on the Ring-bearer
					put("brute", "7_188");      // Morgul Brute: a [wraith] Orc, cost 2, site 4; "When you play this minion, you may spot a Nazgul to add a burden"
					put("ruffian", "7_198");    // Morgul Ruffian: a [wraith] Orc, cost 2; its text bars wounds outside skirmishes while a Nazgul is spotted
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul; his text needs 3 or 5 burdens
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, cost 2, site 6; a skirmish ability only
					put("soldier2", "1_271");
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support condition that stacks a [sauron] Orc and plays one stacked on it
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a skirmish ability used only on purpose
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally, vitality 3; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlungIntotheFrayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Flung Into the Fray
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play a minion stacked here, you may exert a companion.
		 * <b>Regroup:</b> If no minion is stacked here, spot your [wraith] minion to stack it here.
		 * <b>Shadow:</b> Spot 2 [wraith] minions to play a [wraith] minion stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flung");

		assertEquals("Flung Into the Fray", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyAWraithMinionStackedOnItselfNotOneStackedOnAnotherCard() throws DecisionResultInvalidException, CardNotFoundException {
		// F-0488: "a [wraith] minion stacked here" is one stacked on Flung Into the Fray, not on any card.
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var web = scn.GetShadowCard("web");
		var lackey1 = scn.GetShadowCard("lackey1");
		var lackey2 = scn.GetShadowCard("lackey2");
		var onweb = scn.GetShadowCard("onweb");
		var onflung = scn.GetShadowCard("onflung");
		var inhand = scn.GetShadowCard("inhand");

		scn.StartGame();
		scn.MoveCardsToSupportArea(flung, web);
		// The two [wraith] minions to spot
		scn.MoveMinionsToTable(lackey1, lackey2);
		scn.StackCardsOn(web, onweb);
		assertInZone(Zone.STACKED, onweb);
		scn.MoveCardsToHand(inhand);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [wraith] minion is stacked on Web and none on Flung Into the Fray: nothing for it to play.
		assertFalse(scn.ShadowActionAvailable(flung));

		// One is stacked here as well, and the Shadow player is asked again after a play from hand.
		scn.StackCardsOn(flung, onflung);
		assertInZone(Zone.STACKED, onflung);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(inhand);
		assertTrue(scn.ShadowActionAvailable(flung));

		// The one stacked here is the only one it can play, and is played with nothing asked.
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, onflung);
		assertInZone(Zone.STACKED, onweb);
		assertTrue(scn.GetStackedCards(web).contains(onweb));
		// "When you play a minion stacked here, you may exert a companion."
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
	}

	@Test
	public void ShadowAbilityPlaysAWraithMinionStackedHereAsIfFromHandAndItsPlayMayExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var brute = scn.GetShadowCard("brute");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(maggot);
		// Vitality 3: with 2 wounds Boromir is exhausted.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.MoveCardsToSupportArea(flung);
		scn.MoveMinionsToTable(cantea, hound);
		scn.StackCardsOn(flung, brute);
		assertInZone(Zone.STACKED, brute);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsPass();

		// Two [wraith] minions to spot, and a [wraith] minion stacked here.
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);
		// The one minion stacked here is played with nothing asked.
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertEquals(0, scn.GetStackedCards(flung).size());

		// As if played from hand: the Brute's own "When you play this minion" is offered beside this card's trigger.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
		scn.ShadowChooseOptionalTrigger(flung);

		// Companions of other cultures and races, the Ring-bearer among them; not the exhausted Boromir, the ally
		// or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, maggot, cantea, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(2, scn.GetWoundsOn(boromir));
		// The Brute's own trigger is still to be answered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheExertionMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var brute = scn.GetShadowCard("brute");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.MoveCardsToSupportArea(flung);
		// A Nazgul stacked here, so that the play is answered for a minion of either race (the Brute is an Orc).
		scn.MoveMinionsToTable(brute, hound);
		scn.StackCardsOn(flung, cantea);
		assertInZone(Zone.STACKED, cantea);

		scn.FreepsPass();
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPlayedFromHandOrFromAnotherCardsStackIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var teeth = scn.GetShadowCard("teeth");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.MoveCardsToSupportArea(flung, teeth);
		scn.StackCardsOn(teeth, soldier);
		scn.MoveCardsToHand(hound);
		assertInZone(Zone.STACKED, soldier);
		assertInHand(hound);

		scn.FreepsPass();

		// A minion played from the stack of another card.
		assertTrue(scn.ShadowActionAvailable(teeth));
		scn.ShadowUseCardAction(teeth);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(flung));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion played from hand.
		scn.ShadowPlayCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(flung));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertThePlayIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var brute = scn.GetShadowCard("brute");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		// Both companions exhausted: Frodo 3 wounds of 4, Aragorn 3 of 4.  The ally can exert and is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.MoveCardsToSupportArea(flung);
		scn.MoveMinionsToTable(cantea, brute);
		scn.StackCardsOn(flung, hound);
		assertInZone(Zone.STACKED, hound);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(maggot));

		scn.FreepsPass();
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(flung));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityStacksYourWraithMinionHereOnlyWhileNoMinionIsStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var teeth = scn.GetShadowCard("teeth");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");

		scn.StartGame();

		scn.MoveCardsToSupportArea(flung, teeth);
		// A minion stacked on another card does not count as one stacked here.
		scn.StackCardsOn(teeth, soldier2);
		assertInZone(Zone.STACKED, soldier2);

		// No minion in play in the Shadow phase: no skirmishes, straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		// The minions are laid in the regroup phase, as if they had lived through the skirmishes.
		scn.MoveMinionsToTable(cantea, hound, khamul, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(flung).size());
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);

		// A [wraith] Nazgul and a [wraith] Orc; not a Nazgul of another culture or an Orc of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cantea, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInZone(Zone.STACKED, hound);
		assertTrue(scn.GetStackedCards(flung).contains(hound));
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);

		// A minion is stacked here now: with Cantëa still there to spot, the ability is not offered again.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));
	}

	@Test
	public void RegroupAbilityIsOfferedOnAWraithNazgulAloneAndAStackedNazgulBlocksIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();

		scn.MoveCardsToSupportArea(flung);
		scn.SkipToPhase(Phase.REGROUP);
		// The only [wraith] minion is a Nazgul, beside a Nazgul and an Orc of other cultures.
		scn.MoveMinionsToTable(cantea, khamul, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(flung).size());
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.STACKED, cantea);
		assertTrue(scn.GetStackedCards(flung).contains(cantea));

		// A [wraith] Orc to spot is laid, but a Nazgul is the minion stacked here.
		scn.MoveMinionsToTable(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));
		assertTrue(scn.GetStackedCards(flung).contains(cantea));
	}

	@Test
	public void RegroupAbilityIsOfferedOnAWraithOrcAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();

		scn.MoveCardsToSupportArea(flung);
		scn.SkipToPhase(Phase.REGROUP);
		// The only [wraith] minion is an Orc, beside a Nazgul and an Orc of other cultures.
		scn.MoveMinionsToTable(hound, khamul, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(flung).size());
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.STACKED, hound);
		assertTrue(scn.GetStackedCards(flung).contains(hound));
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoWraithMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();

		scn.MoveCardsToSupportArea(flung);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(khamul, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));
		assertEquals(0, scn.GetStackedCards(flung).size());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilTwoWraithMinionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var ruffian = scn.GetShadowCard("ruffian");

		scn.StartGame();
		scn.SetTwilight(10);

		scn.MoveCardsToSupportArea(flung);
		// One [wraith] minion, a Nazgul and an Orc of other cultures beside it, and a [wraith] minion stacked
		// here, which is not in play and cannot be spotted.
		scn.MoveMinionsToTable(cantea, khamul, soldier);
		scn.StackCardsOn(flung, hound);
		scn.MoveCardsToHand(ruffian);
		assertInZone(Zone.STACKED, hound);
		assertInHand(ruffian);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));

		// A second [wraith] minion, played from hand.
		scn.ShadowPlayCard(ruffian);
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		// Its play is answered by this card's "you may exert a companion".
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForTheMinionStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flung = scn.GetShadowCard("flung");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var ruffian = scn.GetShadowCard("ruffian");

		scn.StartGame();

		scn.MoveCardsToSupportArea(flung);
		scn.MoveMinionsToTable(cantea, ruffian);
		scn.StackCardsOn(flung, hound);
		assertInZone(Zone.STACKED, hound);

		scn.FreepsPass();
		// 2 to play the Hound + 2 roaming (site 4 minion at site 2), so 4 is wanted; 3 is one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flung));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(flung));
		scn.ShadowUseCardAction(flung);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		// Its play is answered by this card's "you may exert a companion".
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(flung));
	}

}
