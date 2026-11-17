package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smaug", "30_66");
					// Goblin Runner: [moria] Orc minion, cost 1.  "When you play this minion, you may spot another
					// [moria] card to add (2)": none of these tests plays him.
					put("runner", "31_30");
					// Alfrid, Lickspittle: [men] Man minion, cost 2.  His texts answer an [esgaroth] ally being
					// assigned, a maneuver play from the discard pile and a skirmish ability.
					put("alfrid", "32_25");
					// Thorin, Oakenshield: [dwarven] Dwarf companion.  A fellowship ability and a bonus for the
					// [dwarven] followers he bears.
					put("thorin", "30_19");
					// Dain Ironfoot: [dwarven] Dwarf ally.  Thorin is strength +1; a start-of-regroup trigger
					// that needs a [dwarven] follower, which no test with Dain in it lays.
					put("dain", "32_2");
					// Baïn, Son of Bard: [esgaroth] Man ally who is not Bard.  A start-of-turn heal and a
					// maneuver ability.
					put("bain", "32_15");
					// Bard, The Bowman: [esgaroth] Man ally, the Bard of "except ... Bard".
					put("bard", "30_22");
					// Elrond, Elven Lord: [elven] Elf ally.  A start-of-turn heal and a regroup ability.
					put("elrond", "30_21");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion.  Thorin costs 2 less; Gandalf
					// cannot be discarded beside two [dwarven] followers.  Mirkwood (site 5) discards every
					// Gandalf as the fellowship moves there, so he is laid after that move.
					put("gandalf", "30_26");
					// Bifur, Inarticulate: a [dwarven] follower whose race is Dwarf.
					put("bifur", "30_5");
					// Dwarven Axe: [dwarven] hand weapon possession, bearer a [dwarven] character.
					put("axe", "31_1");
					// Ring of Thrór: [dwarven] artifact of the class Ring, bearer a [dwarven] companion.
					put("ror", "32_7");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SmaugStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Smaug, The Golden
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 16
		 * Type: Minion
		 * Subtype: Dragon
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 7
		 * Game Text: <b>Damage +2</b>. <b>Fierce</b>.<br>Discard Smaug if not at a mountain.<br>For each Dwarf, Man and Elf you spot, Smaug's twilight cost is -1.<br><b>Regroup:</b> Exert Smaug and discard another minion to discard a Free Peoples card (except a companion, Bard or a Ring).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smaug");

		assertEquals("Smaug", card.getBlueprint().getTitle());
		assertEquals("The Golden", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.DRAGON, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(16, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SmaugPlayedAtEsgarothIsDiscardedAsHeComesIntoPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SkipToPhase(Phase.SHADOW);

		// Esgaroth, site 6: a river and a sanctuary, and no mountain.
		var esgaroth = scn.GetCurrentSite();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(esgaroth, Keyword.RIVER));
		assertFalse(scn.HasKeyword(esgaroth, Keyword.MOUNTAIN));

		scn.MoveCardsToHand(smaug);
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.HAND, smaug);
		assertTrue(scn.ShadowPlayAvailable(smaug));

		scn.ShadowPlayCard(smaug);

		assertInZone(Zone.DISCARD, smaug);
		assertNotInZone(Zone.SHADOW_CHARACTERS, smaug);
		// 30 - 16 (nothing to spot but Bilbo) - 2 roaming: what he cost stays paid
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SmaugPlayedAtSmaugsDenStaysInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToPhase(Phase.SHADOW);

		// Smaug's Den, site 8: a mountain and underground, and no river (Erebor, site 7, is a mountain and a river).
		var den = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(den, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(den, Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(den, Keyword.RIVER));

		scn.MoveCardsToHand(smaug);
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.HAND, smaug);

		scn.ShadowPlayCard(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SmaugCostsSixteenWithOnlyAHobbitAWizardAndAnOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var alfrid = scn.GetShadowCard("alfrid");
		var runner = scn.GetShadowCard("runner");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(runner);
		scn.SetShadowHand(smaug, alfrid);
		scn.ShadowRefreshDecision();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, bilbo, gandalf);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(16, scn.GetTwilightCost(smaug));
		// Another minion in hand keeps its own printed cost.
		assertEquals(2, scn.GetTwilightCost(alfrid));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachDwarfManAndElfSpottedOnEitherSideMakesSmaugCostOneLessAndHeStaysAtErebor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var alfrid = scn.GetShadowCard("alfrid");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var bain = scn.GetFreepsCard("bain");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain, bain, elrond);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SUPPORT, dain, bain, elrond);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(runner);
		scn.SetShadowHand(smaug, alfrid);
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();

		// Erebor, site 7: a mountain.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		// A Dwarf companion, a Dwarf ally, a Man ally and an Elf ally; Bilbo, Gandalf and the Orc are none of them.
		assertEquals(12, scn.GetTwilightCost(smaug));
		assertEquals(2, scn.GetTwilightCost(alfrid));

		// The Shadow player's own Man counts as well.
		scn.ShadowPlayCard(alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid);
		assertEquals(11, scn.GetTwilightCost(smaug));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		// 16 - 5 spotted
		assertEquals(pool - 11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFollowerWhoseRaceIsDwarfIsSpottedAsADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var bifur = scn.GetFreepsCard("bifur");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetShadowHand(smaug);
		scn.ShadowRefreshDecision();

		assertEquals(16, scn.GetTwilightCost(smaug));

		// A follower is no character, but a Hobbit Draft Game follower prints a race.
		scn.MoveCardsToSupportArea(bifur);
		assertInZone(Zone.SUPPORT, bifur);
		assertEquals(15, scn.GetTwilightCost(smaug));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsSmaugAndDiscardsAnotherMinionToDiscardAFreePeoplesCardThatIsNoCompanionBardOrRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var alfrid = scn.GetShadowCard("alfrid");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var ror = scn.GetFreepsCard("ror");
		var bain = scn.GetFreepsCard("bain");
		var bard = scn.GetFreepsCard("bard");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe, ror);
		scn.MoveAlliesToTable(bain, bard);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		assertAttachedTo(axe, thorin);
		assertAttachedTo(ror, thorin);
		assertInZone(Zone.SUPPORT, bain, bard, bifur);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner, alfrid);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(smaug));

		// The maneuver phase at Erebor: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		// Bifur's Aid is offered at the start of the phase, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(smaug));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(smaug));

		scn.ShadowUseCardAction(smaug);

		assertEquals(1, scn.GetWoundsOn(smaug));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, alfrid));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(smaug));
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);

		// An ally, a possession and a follower; not a companion, Bard, a Ring, or a Shadow card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bain, axe, bifur));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thorin, bard, ror, alfrid, smaug));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bain);

		assertInZone(Zone.DISCARD, bain);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(ror, thorin);
		assertInZone(Zone.SUPPORT, bard, bifur);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, smaug, alfrid);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNothingButACompanionBardAndARingTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var ror = scn.GetFreepsCard("ror");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, ror);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		assertAttachedTo(ror, thorin);
		assertInZone(Zone.SUPPORT, bard);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(smaug));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(smaug));

		scn.ShadowUseCardAction(smaug);

		assertEquals(1, scn.GetWoundsOn(smaug));
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertAttachedTo(ror, thorin);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileSmaugIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var bain = scn.GetFreepsCard("bain");
		scn.MoveAlliesToTable(bain);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bain);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, runner);
		// Vitality 5 with 4 wounds: exhausted.
		scn.AddWoundsToChar(smaug, 4);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetWoundsOn(smaug));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(smaug));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoOtherMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smaug = scn.GetShadowCard("smaug");
		var bain = scn.GetFreepsCard("bain");
		scn.MoveAlliesToTable(bain);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bain);
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(smaug));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(smaug));
	}
}
