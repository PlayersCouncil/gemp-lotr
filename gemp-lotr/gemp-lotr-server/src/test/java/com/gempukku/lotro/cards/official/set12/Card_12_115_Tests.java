package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glimmer", "12_115");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion; his trigger answers a [rohan] Man exerting
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3
					// Bounder: [shire] Hobbit ally, home site 2 of the Fellowship path, where he may be assigned; his
					// skirmish ability is used only on purpose.
					put("bounder", "1_286");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5; its trigger is on being played
					put("scout", "1_191");      // Moria Scout: strength 6, vitality 2; its trigger is on being played
					// Ted Sandyman, Chief's Men's Ally: a minion of strength 3; his abilities are used only on purpose.
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoldenGlimmerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Golden Glimmer
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Response:</b> If a companion is about to take a wound, spot a [rohan] companion and add 2 burdens to prevent that.
		*/

		var scn = GetScenario();

		var glimmer = scn.GetFreepsCard("glimmer");

		assertEquals("Golden Glimmer", glimmer.getBlueprint().getTitle());
		assertNull(glimmer.getBlueprint().getSubtitle());
		assertFalse(glimmer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, glimmer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, glimmer.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, glimmer.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(glimmer, Keyword.SUPPORT_AREA));
		assertEquals(1, glimmer.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithARohanManCompanionToSpotTwoBurdensPreventTheWoundAboutToBeTakenAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimmer = scn.GetFreepsCard("glimmer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToSupportArea(glimmer);
		assertInZone(Zone.SUPPORT, glimmer);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));

		// 9 against 8: Aragorn loses, and the damage +1 winner gives him two wounds, one at a time.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimmer));
		scn.FreepsAcceptOptionalTrigger();
		// The Guard is spotted, not exerted; two burdens are added.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// "That" is the one wound: the second is answered afresh, and declined it lands.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimmer));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithARohanAllyAndAGondorCompanionItIsNotOfferedAndTheWoundLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimmer = scn.GetFreepsCard("glimmer");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(glimmer);
		assertInZone(Zone.SUPPORT, glimmer, guma);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 13 against 8: a loss, not an overwhelming. A [rohan] Man who is no companion, and a companion who is not
		// [rohan]: nothing to spot.
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glimmer));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyACompanionsWoundIsPreventedAndNotAnAllysOrAMinionsWithARohanHobbitSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimmer = scn.GetFreepsCard("glimmer");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(glimmer);
		assertInZone(Zone.SUPPORT, glimmer);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, runner, ted);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{sam, runner},
				new PhysicalCardImpl[]{bounder, ted});
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(sam));

		// 8 against 6: the minion loses and takes the wound; no offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glimmer));

		// 2 against 3: the ally at his home site loses and takes the wound; no offer.
		scn.FreepsResolveSkirmish(bounder);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glimmer));
		assertEquals(0, scn.GetBurdens());

		// 3 against 5: the [shire] Hobbit companion loses, and with Merry, a [rohan] Hobbit, to spot his wound is
		// prevented for two burdens.
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimmer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
