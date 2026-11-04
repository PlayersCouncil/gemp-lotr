package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_202_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("squad", "11_202");
					put("squad2", "11_202");
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, damage +1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4, resistance 6;
					                            // a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, resistance 5; abilities used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0), strength 4, vitality 2;
					                            // "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SquadofUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Squad of Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is skirmishing a character who has resistance 4 or less, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("squad");

		assertEquals("Squad of Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1MoreOnlyWhileSkirmishingACharacterOfResistance4OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squad = scn.GetShadowCard("squad");
		var squad2 = scn.GetShadowCard("squad2");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(squad, squad2, cuguard);

		// One burden: Aragorn 6 - 1 = 5, Sam 5 - 1 = 4.
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, squad}, new PhysicalCardImpl[]{sam, squad2});
		scn.ShadowAssignToMinions(sam, cuguard);
		assertEquals(1, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(squad2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(aragorn);
		// Against resistance 5: as printed.  The other copy is assigned to Sam (resistance 4) but is not yet
		// skirmishing him: as printed too.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(1, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(squad2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));

		// The copy (10) beats Aragorn (8): his vitality 4 takes the two wounds of damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(sam);

		// Skirmishing Sam, resistance 4: 1 + 1.  The Guard in the same skirmish, and the copy now in no
		// skirmish, are as printed.
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(2, scn.GetKeywordCount(squad2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsDamagePlus1MoreSkirmishingAnAllyWhoHasNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squad = scn.GetShadowCard("squad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(squad);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetKeywordCount(squad, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, squad);
		scn.FreepsResolveSkirmish(thrarin);

		// An ally's resistance is 0 (CR 4.2 "resistance"): 1 + 1.
		assertEquals(0, scn.GetResistance(thrarin));
		assertEquals(2, scn.GetKeywordCount(squad, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
