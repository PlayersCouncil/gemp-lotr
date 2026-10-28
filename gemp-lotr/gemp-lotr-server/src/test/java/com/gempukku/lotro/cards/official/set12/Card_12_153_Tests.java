package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_153_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pikeman", "12_153");
					put("pikeman2", "12_153");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc of strength 13 with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, vitality 4, resistance 6
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, resistance 5
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally, vitality 4, home site 3, where he may skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukPikemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Pikeman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is skirmishing an exhausted companion, this minion is strength +3 (or +5 if that companion has resistance 4 or less).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pikeman");

		assertEquals("Uruk Pikeman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusThreeOnceTheCompanionHeIsSkirmishingIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikeman = scn.GetShadowCard("pikeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pikeman);
		scn.AddWoundsToChar(aragorn, 2);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsExhausted(aragorn));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pikeman);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(pikeman));

		// Ettenmoors' skirmish ability: Aragorn exerts, and is exhausted.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));

		// Resistance 6: +3.
		assertEquals(11, scn.GetStrength(pikeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusFiveAgainstAnExhaustedCompanionOfResistance4AndOnlyInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikeman = scn.GetShadowCard("pikeman");
		var pikeman2 = scn.GetShadowCard("pikeman2");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(pikeman, pikeman2, troop);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 3);

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetResistance(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pikeman}, new PhysicalCardImpl[]{frodo, pikeman2});
		// The minion left over goes to Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(pikeman));
		// Another minion in the same skirmish gains nothing.
		assertEquals(13, scn.GetStrength(troop));
		// The copy assigned to Frodo is not skirmishing yet.
		assertEquals(8, scn.GetStrength(pikeman2));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(frodo);
		// The exhausted Ring-bearer, at resistance 8: +3.
		assertEquals(11, scn.GetStrength(pikeman2));
		assertEquals(8, scn.GetStrength(pikeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusThreeAgainstResistance5ThoughAnotherCompanionHas4AndNothingAgainstAnExhaustedAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikeman = scn.GetShadowCard("pikeman");
		var pikeman2 = scn.GetShadowCard("pikeman2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(pikeman, pikeman2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(elrond, 3);

		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(elrond));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));

		// On to Rivendell Valley, site 3: Elrond's home.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pikeman}, new PhysicalCardImpl[]{elrond, pikeman2});
		scn.FreepsResolveSkirmish(aragorn);

		// Resistance 5: +3, whatever Sam's is.
		assertEquals(11, scn.GetStrength(pikeman));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(elrond);
		// An exhausted ally is no exhausted companion.
		assertEquals(8, scn.GetStrength(pikeman2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
