package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haradrim", "11_80");
					// Footman of Dunland: a [men] Man minion; "the fellowship's current site gains plains" reaches no reading
					put("footman", "11_82");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
					// Ulaire Attea, The Easterling: [wraith] Nazgul, "When you play Ulaire Attea, you may wound an ally"
					put("attea", "2_82");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion of vitality 1, no text
					// Fierce in Despair: [raider] Regroup event, "Spot 2 [raider] Men to discard a companion (except the
					// Ring-bearer)"; the Easterling Axemen are the Men, with no text below 2 burdens
					put("despair", "7_148");
					put("axeman1", "4_224");
					put("axeman2", "4_224");
					// Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)"
					put("slayer", "3_93");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, his ability used only on purpose
					// Defend It and Hope: [gondor] Maneuver event, "Exert a [gondor] Man to wound a minion once"
					put("defend", "4_115");
					put("guard1", "1_7");       // Dwarf Guard: strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally of vitality 2, his ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FerociousHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Ferocious Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>Each time a Free Peoples character is killed, each of your [men] minions is strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haradrim");

		assertEquals("Ferocious Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AnAllyKilledMakesEachOfYourMenMinionsStrengthPlus3AndNotOnePlayedAfterwards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradrim = scn.GetShadowCard("haradrim");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var attea = scn.GetShadowCard("attea");
		var footman = scn.GetShadowCard("footman");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(attea, footman);

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, ted, troop);
		scn.AddWoundsToChar(thrarin, 1);
		scn.SetTwilight(20);

		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		// Thrarin is the one ally, wounded unasked: the wound takes the last of his vitality.
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DEAD, thrarin);

		// Each [men] minion, a Man and a Hobbit; not a minion of another culture.
		assertEquals(15, scn.GetStrength(haradrim));
		assertEquals(6, scn.GetStrength(ted));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [men] minion played after the kill is not reached: the bonus went to the minions then in play.
		scn.ShadowPlayCard(footman);
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(15, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCompanionKilledAddsPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradrim = scn.GetShadowCard("haradrim");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, footman);
		scn.AddWoundsToChar(guard1, 1);

		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(7, scn.GetStrength(footman));

		// The Haradrim is the one archer: its wound takes the last of the first Dwarf Guard's vitality.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(guard1);
		assertInZone(Zone.DEAD, guard1);
		assertEquals(15, scn.GetStrength(haradrim));
		assertEquals(10, scn.GetStrength(footman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard2, haradrim}, new PhysicalCardImpl[]{aragorn, footman});
		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		// 15 against 4 is double: the second Dwarf Guard is overwhelmed and killed.
		assertInZone(Zone.DEAD, guard2);
		assertEquals(18, scn.GetStrength(haradrim));
		assertEquals(13, scn.GetStrength(footman));

		// Still in force in the next skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(18, scn.GetStrength(haradrim));
		assertEquals(13, scn.GetStrength(footman));

		// 13 against 8: Aragorn loses and takes a wound, and lives.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(7, scn.GetStrength(footman));
	}

	@Test
	public void ACompanionDiscardedSetsNothingOffAndOneKilledInTheSamePhaseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradrim = scn.GetShadowCard("haradrim");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		var slayer = scn.GetShadowCard("slayer");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Laid in the regroup phase, so that no archery or skirmish comes before.
		scn.MoveMinionsToTable(haradrim, axeman1, axeman2, slayer);
		scn.AddWoundsToChar(guard2, 1);
		scn.SetTwilight(10);

		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(12, scn.GetStrength(haradrim));

		scn.FreepsPass();
		scn.ShadowPlayCard(despair);
		scn.ShadowChooseCard(guard1);
		// Discarded from play, not killed.
		assertInZone(Zone.DISCARD, guard1);
		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(6, scn.GetStrength(axeman1));

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		// The Slayer's wound takes the last of the second Dwarf Guard's vitality: he is killed.
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInZone(Zone.DEAD, guard2);
		assertEquals(15, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haradrim = scn.GetShadowCard("haradrim");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(12, scn.GetStrength(haradrim));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(brigand);
		// Aragorn paid the exertion, and the Brigand's one vitality is gone: he is killed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(12, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
