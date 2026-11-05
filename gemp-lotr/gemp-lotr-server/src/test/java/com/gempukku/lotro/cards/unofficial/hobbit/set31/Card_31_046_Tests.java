package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_046_Tests
{

	// The Hobbit table, with Forest River as site 5 in place of Mirkwood: SkipToSite(4) ends at Goblin Town (site 4),
	// and the move from there is to Forest River.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] companion; a Fellowship ability not used
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, vitality 4; a Fellowship ability not used
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, no ally (no Hobbit-set one is printed)
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] ally; a start-of-turn heal and a Regroup ability, not used
					put("hadafang", "33_13");   // Hadafang: on Elrond; with no Gandalf in play he may skirmish at site 5
					put("thranduil", "31_12");  // Thranduil, Elven King: [elven] ally; a Maneuver ability not used
					put("bard", "30_22");       // Bard, The Bowman: an [esgaroth] ally; a Regroup ability not used

					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc, vitality 2; a Response at a battleground only
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, vitality 2; text only at site 9 and in the regroup phase
					put("goblin", "31_36");     // The Great Goblin: [moria] Orc printed fierce, strength 10, vitality 3; a Maneuver ability not used
					put("bats", "32_30");       // Dark Bats: [moria] minion, a Creature and no Orc, vitality 2; a Shadow ability not used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site5", "31_46");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ForestRiverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Forest River
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 5
		 * Shadow Number: 7
		 * Direction: none
		 * Game Text: <b>Forest</b>. <b>River</b>. <b>Assignment:</b> Exert your Orc to prevent Gandalf or an [elven] ally from being assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Forest River", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AssignmentExertsYourOrcToPreventGandalfFromBeingAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var thorin = scn.GetFreepsCard("thorin");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var thranduil = scn.GetFreepsCard("thranduil");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();

		var assaulting = scn.GetShadowCard("assaulting");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, avenger, thorin, arwen);
		scn.MoveAlliesToTable(elrond, thranduil, bard);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, avenger, thorin, arwen);
		scn.FreepsPass();
		var river = scn.GetCurrentSite();
		assertEquals("31_46", river.getBlueprintId());
		scn.MoveMinionsToTable(assaulting, veteran, bats);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsCanAssign(gandalf));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.ShadowActionAvailable(river));
		scn.ShadowUseCardAction(river);

		// His Orcs of two cultures; not Dark Bats, a minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(assaulting, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(veteran);
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(bats));

		// Gandalf, and [elven] allies of two titles; not a [gandalf] companion who is no Gandalf, an [elven]
		// companion, an ally of another culture, or another companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, elrond, thranduil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(avenger, arwen, bard, thorin, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gandalf);

		scn.PassAssignmentActions();
		// Neither player may assign Gandalf; the others may be.
		assertFalse(scn.FreepsCanAssign(gandalf));
		assertFalse(scn.ShadowCanAssign(gandalf));
		assertTrue(scn.FreepsCanAssign(thorin));
		assertTrue(scn.FreepsCanAssign(avenger));
		assertTrue(scn.FreepsCanAssign(arwen));
		scn.FreepsAssignToMinions(thorin, assaulting);
		assertTrue(scn.ShadowCanAssign(avenger));
		scn.ShadowAssignToMinions(avenger, veteran);

		assertTrue(scn.IsCharAssignedAgainst(thorin, assaulting));
		assertTrue(scn.IsCharAssignedAgainst(avenger, veteran));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentPreventsAnElvenAllyFromBeingAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");

		var assaulting = scn.GetShadowCard("assaulting");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		assertAttachedTo(hadafang, elrond);
		scn.FreepsPass();
		var river = scn.GetCurrentSite();
		scn.MoveMinionsToTable(assaulting);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With no Gandalf, Hadafang lets Elrond skirmish at site 5.
		assertTrue(scn.FreepsCanAssign(elrond));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(assaulting));
		scn.ShadowUseCardAction(river);
		// The one Orc exerts, and the one [elven] ally is chosen, with no question.
		assertEquals(1, scn.GetWoundsOn(assaulting));

		scn.PassAssignmentActions();
		assertFalse(scn.FreepsCanAssign(elrond));
		assertTrue(scn.FreepsCanAssign(thorin));
		scn.FreepsAssignToMinions(thorin, assaulting);
		assertFalse(scn.IsCharAssigned(elrond));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ThePreventionLastsTheAssignmentPhaseAndGandalfIsAssignedInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var thorin = scn.GetFreepsCard("thorin");

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, thorin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, thorin);
		scn.FreepsPass();
		var river = scn.GetCurrentSite();
		scn.MoveMinionsToTable(goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(goblin));
		scn.ShadowUseCardAction(river);
		// The Great Goblin, the one Orc, pays, and Gandalf, the one candidate, is chosen with no question.
		assertEquals(1, scn.GetWoundsOn(goblin));

		scn.PassAssignmentActions();
		assertFalse(scn.FreepsCanAssign(gandalf));
		scn.FreepsAssignToMinions(thorin, goblin);
		// Strength 10 against 7: Thorin loses and takes a wound.
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(thorin));

		// The fierce assignment phase is a phase of its own: Gandalf may be assigned again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssign(gandalf));
		scn.FreepsAssignToMinions(gandalf, goblin);
		assertTrue(scn.IsCharAssignedAgainst(gandalf, goblin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var assaulting = scn.GetShadowCard("assaulting");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		scn.FreepsPass();
		var river = scn.GetCurrentSite();
		scn.MoveMinionsToTable(assaulting, bats);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(river));
	}

	@Test
	public void AssignmentIsNotOfferedWhileTheOnlyOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var assaulting = scn.GetShadowCard("assaulting");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		scn.FreepsPass();
		var river = scn.GetCurrentSite();
		scn.MoveMinionsToTable(assaulting, bats);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Vitality 2 with 1 wound: the Assaulting Goblin cannot exert.  Dark Bats, a minion who is no Orc, can.
		scn.AddWoundsToChar(assaulting, 1);
		assertEquals(1, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(bats));
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(assaulting));

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(river));
	}
}
