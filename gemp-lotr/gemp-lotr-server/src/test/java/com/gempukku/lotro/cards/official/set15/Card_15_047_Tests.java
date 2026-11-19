package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ntt", "15_47");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum], strength 5, vitality 4; a regroup ability used only on purpose
					put("wgollum", "103_66");   // Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; his abilities need a Nazgul played or are used only on purpose
					put("shelob", "8_25");      // Shelob, Eater of Light: a [gollum] minion who is no Gollum, vitality 8; cheated in, so her play text never comes up
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; cheated in, so its text never comes up
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who may be made to skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotThisTimeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Not This Time!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Exert Gollum and remove a threat to wound a companion Gollum is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ntt");

		assertEquals("Not This Time!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityExertsGollumAndRemovesAThreatToWoundTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var ntt = scn.GetShadowCard("ntt");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(gollum);
		// Frodo and Aragorn: room for 2 threats.
		scn.AddThreats(2);
		assertInZone(Zone.SUPPORT, ntt);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowActionAvailable(ntt));
		scn.ShadowUseCardAction(ntt);

		// One Gollum and one companion skirmishing him: both are taken without asking.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, ntt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWraithGollumPaysTooAndAnExhaustedCompanionHeIsSkirmishingIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		var ntt = scn.GetShadowCard("ntt");
		var wgollum = scn.GetShadowCard("wgollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(wgollum);
		// Merry, vitality 4, exhausted: an exertion could not be placed on him, a wound kills him.
		scn.AddWoundsToChar(merry, 3);
		// One threat only: a threat left over when a companion is killed would become a wound on Frodo.
		scn.AddThreats(1);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, wgollum);
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(wgollum));
		assertTrue(scn.ShadowActionAvailable(ntt));
		scn.ShadowUseCardAction(ntt);

		assertEquals(1, scn.GetWoundsOn(wgollum));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DEAD, merry);
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		// His only opponent gone, the skirmish is over, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithGollumExhaustedItIsNotOfferedThoughAnotherGollumMinionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var ntt = scn.GetShadowCard("ntt");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(gollum, shelob);
		// Gollum, vitality 4, exhausted; Shelob, a [gollum] minion of another title, could exert.
		scn.AddWoundsToChar(gollum, 3);
		scn.AddThreats(2);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{merry, shelob});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ntt));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WithNoThreatItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var ntt = scn.GetShadowCard("ntt");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ntt));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void UsedWhileGollumWaitsForAnotherSkirmishItIsPaidForAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);

		var ntt = scn.GetShadowCard("ntt");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(gollum, runner);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{merry, gollum});
		// Aragorn's skirmish first: Merry is assigned to Gollum, and not yet skirmishing him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(ntt));
		scn.ShadowUseCardAction(ntt);

		// The cost is paid; the companion is part of the effect, and there is none.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyGollumIsSkirmishingIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var ntt = scn.GetShadowCard("ntt");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ntt);
		scn.MoveMinionsToTable(gollum);
		// Frodo alone: room for 1 threat.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, gollum);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, gollum));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowActionAvailable(ntt));
		scn.ShadowUseCardAction(ntt);

		// Thrarin, with one vitality left, is still there and unwounded by it.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, thrarin);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
