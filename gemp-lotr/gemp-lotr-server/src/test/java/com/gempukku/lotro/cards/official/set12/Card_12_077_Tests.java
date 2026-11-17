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

public class Card_12_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trident", "12_77");
					put("trident2", "12_77");
					put("men", "11_100");       // Strange-looking Men: [men] Man minion, strength 10, vitality 2; its ability is used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; his abilities are used only on purpose
					put("axeman", "11_69");     // Axeman of Harad: [men] Man minion, strength 11; a cost reduction only
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, resistance 6; his ability is used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0); Maneuver: exert him to take part in skirmishes until the regroup phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarTridentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: War Trident
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [men] minion.<br>While bearer is skirmishing a companion who has resistance 5 or less, bearer is strength +2.<br>While bearer is skirmishing a companion who has resistance 3 or less, bearer is strength +2.
		*/

		var scn = GetScenario();

		var trident = scn.GetFreepsCard("trident");

		assertEquals("War Trident", trident.getBlueprint().getTitle());
		assertNull(trident.getBlueprint().getSubtitle());
		assertFalse(trident.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, trident.getBlueprint().getSide());
		assertEquals(Culture.MEN, trident.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, trident.getBlueprint().getCardType());
		assertTrue(trident.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, trident.getBlueprint().getTwilightCost());
		assertEquals(2, trident.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMenMinionOfEitherRaceAndNotOnAnotherCulturesMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trident = scn.GetShadowCard("trident");
		var men = scn.GetShadowCard("men");
		var ted = scn.GetShadowCard("ted");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trident);

		scn.StartGame();
		assertInHand(trident);
		scn.MoveMinionsToTable(men, ted, easterling);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(trident));
		scn.ShadowPlayCard(trident);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(men, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, aragorn));
		scn.ShadowChooseCard(ted);
		assertAttachedTo(trident, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StrengthPlus2AgainstACompanionAtResistance5AndPlus4AtResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trident = scn.GetShadowCard("trident");
		var men = scn.GetShadowCard("men");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(men);
		scn.AttachCardsTo(men, trident);
		assertAttachedTo(trident, men);
		scn.AddBurdens(4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, men);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Frodo 10 - 4 burdens: 6, too high; the Men read 10 + 2 for the Trident's printed bonus.
		assertEquals(6, scn.GetResistance(frodo));
		assertEquals(12, scn.GetStrength(men));

		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(frodo));
		assertEquals(14, scn.GetStrength(men));

		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(frodo));
		assertEquals(14, scn.GetStrength(men));

		scn.AddBurdens(1);
		assertEquals(3, scn.GetResistance(frodo));
		assertEquals(16, scn.GetStrength(men));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyTheBearerSkirmishingALowResistanceCompanionIsStrongerAndNotAgainstAnAllyOrWhileWaiting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trident = scn.GetShadowCard("trident");
		var trident2 = scn.GetShadowCard("trident2");
		var men = scn.GetShadowCard("men");
		var ted = scn.GetShadowCard("ted");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(men, ted, axeman);
		scn.AttachCardsTo(men, trident);
		scn.AttachCardsTo(ted, trident2);
		assertAttachedTo(trident, men);
		assertAttachedTo(trident2, ted);
		scn.AddBurdens(3);
		// Aragorn 6 - 3 burdens: 3; an ally has resistance 0.
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, men}, new PhysicalCardImpl[]{thrarin, ted});
		scn.ShadowAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(thrarin);

		// Ted, 3 + 2 printed, against an ally, who is no companion; the Men, assigned to Aragorn and waiting for
		// that skirmish, read 10 + 2 printed.
		assertTrue(scn.IsCharSkirmishingAgainst(ted, thrarin));
		assertEquals(5, scn.GetStrength(ted));
		assertEquals(12, scn.GetStrength(men));
		assertEquals(11, scn.GetStrength(axeman));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		// 10 + 2 printed + 2 + 2 against Aragorn at resistance 3; the Axeman beside him bears no Trident.
		assertTrue(scn.IsCharSkirmishingAgainst(men, aragorn));
		assertEquals(16, scn.GetStrength(men));
		assertEquals(11, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
