package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_040_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow and
	// regroup phases of turn 1 are at The Troll Hoard (site 2); a regroup move takes the fellowship on to Rivendell
	// (site 3), whose only text bars Trolls being played. The minions are laid in the Shadow phase and live through
	// an assignment phase in which nobody is assigned.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "32_40");

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc, vitality 3; a maneuver ability at a battleground
					put("swarg", "31_25");      // Savage Warg: a mount borne by an Orc, vitality +1 (Azog at 4); fierce, a win trigger
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, vitality 2; its regroup ability not used
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, no Orc, vitality 4; his assignment ability not used
					put("sceptre", "33_43");    // Great Goblin's Sceptre: a possession that is no mount, borne by a unique Orc
					put("vwarg", "32_32");      // Vicious Warg: a mount borne by an Orc; its play trigger is never met (cheated on)

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a fellowship ability not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion; a skirmish ability not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheOrcArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: The Orc Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: At the start of the regroup phase, you may heal each Orc twice.
		 * <b>Regroup:</b> If this minion is not mounted, exert him to make the Free Peoples player wound a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("The Orc Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseMayHealEachOrcTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var azog = scn.GetShadowCard("azog");
		var swarg = scn.GetShadowCard("swarg");
		var veteran = scn.GetShadowCard("veteran");
		var sauron = scn.GetShadowCard("sauron");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army, azog, veteran, sauron);
		scn.AttachCardsTo(azog, swarg);
		// Vitality 3 + 1 with 3 wounds: Azog keeps a wound after two heals, so "twice" is told from "every wound".
		scn.AddWoundsToChar(azog, 3);
		scn.AddWoundsToChar(army, 2);
		scn.AddWoundsToChar(veteran, 1);
		scn.AddWoundsToChar(sauron, 2);
		scn.AddWoundsToChar(balin, 1);
		assertAttachedTo(swarg, azog);
		assertEquals(3, scn.GetWoundsOn(azog));
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(2, scn.GetWoundsOn(sauron));
		assertEquals(1, scn.GetWoundsOn(balin));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(army));
		scn.ShadowAcceptOptionalTrigger();
		// Orcs of two cultures, the Orc Army itself among them; not a [sauron] minion who is no Orc, nor a companion.
		assertEquals(1, scn.GetWoundsOn(azog));
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(2, scn.GetWoundsOn(sauron));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoOfferAtTheStartOfTheManeuverPhaseAndDecliningAtRegroupHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army, veteran);
		scn.AddWoundsToChar(army, 2);
		scn.AddWoundsToChar(veteran, 1);
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(1, scn.GetWoundsOn(veteran));

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(army));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(1, scn.GetWoundsOn(veteran));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(army));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoOfferWithNoWoundedOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var sauron = scn.GetShadowCard("sauron");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army, sauron);
		scn.AddWoundsToChar(sauron, 2);
		scn.AddWoundsToChar(balin, 1);
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(2, scn.GetWoundsOn(sauron));
		assertEquals(1, scn.GetWoundsOn(balin));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(army));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(sauron));
		assertEquals(1, scn.GetWoundsOn(balin));
	}

	@Test
	public void TheOfferIsMadeAgainAtTheSecondRegroupPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army);
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(army));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(army));

		// The fellowship moves on to Rivendell (site 3); the Orc Army stays in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, army);
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(army));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(army));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionExertsTheOrcArmyToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var sceptre = scn.GetShadowCard("sceptre");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, gloin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 3 with 2 wounds: Balin is exhausted, so a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();
		scn.MoveMinionsToTable(army);
		// A possession that is no mount: the Orc Army is still not mounted.
		scn.AttachCardsTo(army, sceptre);
		assertAttachedTo(sceptre, army);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(army));
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);
		assertEquals(1, scn.GetWoundsOn(army));
		// The Free Peoples player chooses the companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, gloin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileTheOrcArmyIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var vwarg = scn.GetShadowCard("vwarg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army);
		scn.AttachCardsTo(army, vwarg);
		assertAttachedTo(vwarg, army);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army));
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(balin));
	}

	@Test
	public void RegroupActionIsNotOfferedWhileTheOrcArmyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(army);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));

		scn.SkipToPhase(Phase.REGROUP);
		// Its own heal declined, so it stays exhausted.
		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army));
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(balin));
	}
}
