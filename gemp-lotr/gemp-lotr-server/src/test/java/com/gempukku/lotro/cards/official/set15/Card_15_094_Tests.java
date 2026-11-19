package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman1", "15_94");
					put("hillman2", "15_94");    // a second copy: not unique
					put("corps", "11_73");       // Corps of Harad: [men] Man minion, strength 10; fierce only at a battleground (none on this path)
					put("grousing", "15_82");    // Grousing Hillman: [men] Man minion of another title; +3 and fierce only while its owner controls a site
					put("ted", "18_75");         // Ted Sandyman: a [men] Hobbit minion (no Man); abilities used only on purpose
					put("soldier", "4_254");     // Southron Soldier: a [raider] Man minion; Southron, ambush (2)
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("sword", "1_112");       // Ranger's Sword: strength +2, Aragorn only; damage +1
					put("glorfindel", "9_16");   // Glorfindel, Revealed in Wrath: [elven] Elf companion, strength 9; abilities used only on purpose
					put("lindenroot", "5_19");   // Lindenroot, Elder Shepherd: [gandalf] Ent companion, strength 10; unhasty
					put("tom", "9_52");          // Tom Bombadil, The Master: an ally of strength 14; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WanderingHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Wandering Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot a [men] Man.<br>While you can spot a companion of strength 10 or more, this minion is strength +3.<br><b>Maneuver:</b> Remove 2 burdens or 2 threats to control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman1");

		assertEquals("Wandering Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToPlayItNeedsAMenManAndAMenHobbitOrARaiderOrGondorManWillNotDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman1 = scn.GetShadowCard("hillman1");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveCardsToHand(hillman1, corps);
		assertInZone(Zone.HAND, hillman1);
		assertInZone(Zone.HAND, corps);
		scn.MoveMinionsToTable(ted, soldier);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [men] Hobbit, a [raider] Man and a [gondor] Man companion: no [men] Man to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, hillman1);
		assertFalse(scn.ShadowPlayAvailable(hillman1));

		scn.ShadowPlayCard(corps);
		assertInZone(Zone.SHADOW_CHARACTERS, corps);
		assertTrue(scn.ShadowPlayAvailable(hillman1));
		scn.ShadowPlayCard(hillman1);
		assertInZone(Zone.SHADOW_CHARACTERS, hillman1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ToPlayAMenManOfAnotherTitleWillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman1 = scn.GetShadowCard("hillman1");
		var grousing = scn.GetShadowCard("grousing");

		scn.StartGame();
		scn.MoveCardsToHand(hillman1);
		assertInZone(Zone.HAND, hillman1);
		scn.MoveMinionsToTable(grousing);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, hillman1);
		assertTrue(scn.ShadowPlayAvailable(hillman1));
		scn.ShadowPlayCard(hillman1);
		assertInZone(Zone.SHADOW_CHARACTERS, hillman1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileACompanionOfStrength10OrMoreCanBeSpottedThisMinionIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var tom = scn.GetFreepsCard("tom");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveAlliesToTable(tom);
		scn.MoveCardsToHand(sword);

		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman1, hillman2, corps);
		scn.FreepsRefreshDecision();

		// Glorfindel is one short, at 9; Tom Bombadil (14) is an ally and the Corps of Harad (10) a minion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));
		assertEquals(14, scn.GetStrength(tom));
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(8, scn.GetStrength(hillman1));
		assertEquals(8, scn.GetStrength(hillman2));
		assertInZone(Zone.HAND, sword);

		scn.FreepsPlayCard(sword);

		// Aragorn, 8 + 2, is exactly 10: each Hillman is 8 + 3 by its own text, the other copy adding nothing to
		// it; the Corps of Harad, another [men] Man minion, is not touched.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(11, scn.GetStrength(hillman1));
		assertEquals(11, scn.GetStrength(hillman2));
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionOfAnotherCultureAndRacePrintedAtStrength10WillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lindenroot = scn.GetFreepsCard("lindenroot");
		scn.MoveCompanionsToTable(lindenroot);

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman1);

		assertEquals(10, scn.GetStrength(lindenroot));
		// 8 + 3
		assertEquals(11, scn.GetStrength(hillman1));
	}

	@Test
	public void ManeuverAbilityRemoves2BurdensToTakeControlOfTheLowestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		// Both markers on site 3, so that sites 1 and 2 are behind them.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(hillman1);
		scn.AddBurdens(2);
		scn.AddThreats(1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowActionAvailable(hillman1));
		scn.ShadowUseCardAction(hillman1);

		// One threat cannot pay: the burdens are removed with nothing asked, and the lowest site is taken.
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.ShadowControls(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityRemoves2ThreatsWhereTheBurdensCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(hillman1);
		scn.AddBurdens(1);
		scn.AddThreats(2);
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(hillman1));
		scn.ShadowUseCardAction(hillman1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityAsksWhichToRemoveWhereBothCanPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(hillman1);
		scn.AddBurdens(2);
		scn.AddThreats(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(hillman1));
		scn.ShadowUseCardAction(hillman1);

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowGetMultipleChoices().contains("Remove 2 burdens"));
		assertTrue(scn.ShadowGetMultipleChoices().contains("Remove 2 threats"));
		scn.ShadowChoose("Remove 2 threats");

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOneBurdenAndOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(hillman1);
		scn.AddBurdens(1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hillman1));
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheBurdensArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman1 = scn.GetShadowCard("hillman1");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman1);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Turn 1: the Shadow player's marker is still on site 1, so no site is behind every marker.
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowActionAvailable(hillman1));
		scn.ShadowUseCardAction(hillman1);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
