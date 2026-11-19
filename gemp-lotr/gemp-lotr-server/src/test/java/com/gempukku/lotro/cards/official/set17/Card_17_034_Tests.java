package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spirit", "17_34");
					put("spirit2", "17_34");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; his skirmish ability is used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] HOBBIT companion, strength 3, vitality 4; his skirmish ability is used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ALLY; his maneuver ability is used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: another [gondor] condition on a [gondor] companion; its skirmish ability is used only on purpose
					put("swordarm", "1_116");   // Swordarm of the White Tower: Free Peoples Skirmish event, a [gondor] companion strength +2
					put("light", "4_309");      // Light Shining Faintly: Free Peoples Fellowship event: add a burden to heal a Ring-bound companion twice

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("drums", "1_168");      // Drums in the Deep: Shadow Skirmish event, a [moria] Orc strength +2
					put("slayer", "1_153");     // Uruk Slayer: [isengard] Uruk-hai, strength 9, damage +1; Skirmish: remove (1) to be strength +1
					put("slayer2", "1_153");
					put("miscreant", "11_131"); // Orc Miscreant: [orc] Orc; Regroup: exert this minion to add a burden
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a twilight Nazgul; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("wants", "2_78");       // It Wants to be Found: Shadow Maneuver event: spot a twilight Nazgul to add a burden
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; when he is assigned, the Free Peoples player chooses to discard 3 cards or add a burden
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Courtyard Parapet stands in place of site 9, which no test reaches, and a test puts it on the path as the next
	// site the fellowship moves to.
	protected VirtualTableScenario GetSitePathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spirit", "17_34");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; his skirmish ability is used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, held in the Shadow hand
					put("runner2", "1_178");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("parapet", "13_188");   // Courtyard Parapet: a Shadows-block site, "the first Shadow player may discard 2 cards from hand to add a burden"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiritoftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Spirit of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be a [gondor] companion.
		 * 	An opponent can not play skirmish events or use skirmish special abilities during skirmishes involving bearer.
		 * 	Each time a Shadow player adds a burden, discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spirit");

		assertEquals("Spirit of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyOnAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(spirit);

		scn.StartGame();

		assertInHand(spirit);
		assertTrue(scn.FreepsPlayAvailable(spirit));
		scn.FreepsPlayCard(spirit);

		// A [gondor] Man and a [gondor] Hobbit; not the [gondor] ally, nor Frodo, a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(spirit, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerCannotPlayASkirmishEventInBearersSkirmishButCanInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.AttachCardsTo(boromir, spirit);
		scn.MoveCardsToHand(swordarm);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		assertAttachedTo(spirit, boromir);
		assertInHand(drums);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner},
				new PhysicalCardImpl[]{pippin, runner2});

		scn.FreepsResolveSkirmish(boromir);
		// The bearer's own player plays his skirmish event there.
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(swordarm));
		scn.FreepsPlayCard(swordarm);
		scn.FreepsChooseCard(boromir);
		assertEquals(9, scn.GetStrength(boromir));

		// The Shadow player may not play his.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(drums));
		scn.PassCurrentPhaseActions();

		// 9 against 5: the Runner dies.
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsResolveSkirmish(pippin);
		scn.FreepsPass();

		// In Pippin's skirmish he may.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.ShadowPlayAvailable(drums));
		scn.ShadowPlayCard(drums);

		assertEquals(7, scn.GetStrength(runner2));
		assertInZone(Zone.DISCARD, drums);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayerCannotUseASkirmishSpecialAbilityInBearersSkirmishButCanInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.AttachCardsTo(boromir, spirit);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2);
		assertAttachedTo(spirit, boromir);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, slayer},
				new PhysicalCardImpl[]{pippin, slayer2});
		// Twilight in the pool for the abilities.
		assertTrue(scn.GetTwilight() >= 1);

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		scn.ShadowPass();

		// 7 against 9: Boromir loses, and takes 1 wound and 1 more for the damage bonus.
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.FreepsResolveSkirmish(pippin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(slayer2));
		assertTrue(scn.ShadowActionAvailable(slayer2));
		scn.ShadowUseCardAction(slayer2);

		assertEquals(10, scn.GetStrength(slayer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ABurdenAddedByAShadowMinionsAbilityDiscardsEachCopyAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var spirit2 = scn.GetFreepsCard("spirit2");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.AttachCardsTo(boromir, spirit, saga);
		scn.AttachCardsTo(pippin, spirit2);

		var miscreant = scn.GetShadowCard("miscreant");

		scn.StartGame();
		// No minion: the game goes on to the regroup phase, where a minion that lived through the skirmishes is laid.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(miscreant);
		assertAttachedTo(spirit, boromir);
		assertAttachedTo(saga, boromir);
		assertAttachedTo(spirit2, pippin);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(miscreant));
		scn.ShadowUseCardAction(miscreant);

		assertEquals(1, scn.GetWoundsOn(miscreant));
		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.DISCARD, spirit, spirit2);
		// Another [gondor] condition stays.
		assertAttachedTo(saga, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABurdenAddedByAShadowEventDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, spirit);

		var nelya = scn.GetShadowCard("nelya");
		var wants = scn.GetShadowCard("wants");
		scn.MoveCardsToHand(wants);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		assertAttachedTo(spirit, boromir);
		assertInHand(wants);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(wants));
		scn.ShadowPlayCard(wants);

		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.DISCARD, spirit, wants);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ABurdenTheFreePeoplesPlayerAddsDoesNotDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		var light = scn.GetFreepsCard("light");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, spirit);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertAttachedTo(spirit, boromir);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(light);

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertAttachedTo(spirit, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenTheFreePeoplesPlayerChoosesUnderAShadowCardDoesNotDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		var light = scn.GetFreepsCard("light");
		var swordarm = scn.GetFreepsCard("swordarm");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, spirit);
		// Three cards in hand, so that "discard 3 cards from hand" can be chosen too.
		scn.MoveCardsToHand(light, swordarm, saga);

		var runner = scn.GetShadowCard("runner");
		var defense = scn.GetShadowCard("defense");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(boromir, defense);
		assertAttachedTo(spirit, boromir);
		assertAttachedTo(defense, boromir);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);

		// The Free Peoples player has the choice, and so adds the burden himself (Q-0171).
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertAttachedTo(spirit, boromir);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ABurdenAShadowPlayerAddsByASitesTextDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSitePathScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, spirit);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// Courtyard Parapet becomes site 2.
		var parapet = scn.GetShadowSite("parapet");
		scn.MoveCardToAdventurePath(parapet);
		scn.MoveCardsToHand(runner, runner2);
		assertAttachedTo(spirit, boromir);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPass();

		assertSame(parapet, scn.GetCurrentSite());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(parapet));
		scn.ShadowAcceptOptionalTrigger();

		// The Shadow player adds the burden by a site's text, which is no card of his side.
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.DISCARD, spirit);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
