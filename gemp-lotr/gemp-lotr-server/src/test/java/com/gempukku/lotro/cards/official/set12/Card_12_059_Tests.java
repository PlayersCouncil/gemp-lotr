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

public class Card_12_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("covetous", "12_59");
					// A second copy, in another skirmish.
					put("covetous2", "12_59");
					// Dunlending Brigand: a [dunland] Man minion with no text, strength 8, vitality 1.
					put("brigand", "4_10");

					// Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4.  His maneuver
					// ability is never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: a Dwarf companion, strength 6, vitality 3, damage +1 of his own.  His
					// skirmish ability is never used.
					put("gimli", "1_13");
					// Ottar, Man of Laketown: a [gandalf] Man ally, home site 3, where he can be assigned.  "To play,
					// spot Gandalf" is passed over by the cheat, and his fellowship ability is never used.
					put("ottar", "1_80");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CovetousEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Covetous Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is skirmishing a Man, it is <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("covetous");

		assertEquals("Covetous Easterling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus2OnlyWhileItIsItselfSkirmishingAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var covetous = scn.GetShadowCard("covetous");
		var covetous2 = scn.GetShadowCard("covetous2");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(covetous, covetous2, brigand);
		assertEquals(0, scn.GetKeywordCount(covetous, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, covetous}, new PhysicalCardImpl[]{gimli, covetous2});
		scn.ShadowAssignToMinions(aragorn, brigand);
		assertEquals(0, scn.GetKeywordCount(covetous, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(covetous2, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(gimli);
		// Skirmishing a Dwarf: no bonus.  The other copy is assigned to Aragorn but is not yet skirmishing him: no
		// bonus either.
		assertEquals(0, scn.GetKeywordCount(covetous2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(covetous, Keyword.DAMAGE));

		// 10 beats Gimli's 6, not double: one wound, as a minion of no damage bonus places.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);

		// Skirmishing Aragorn, a Man: damage +2.  The Brigand in the same skirmish, and the copy now in no
		// skirmish, have none.
		assertEquals(2, scn.GetKeywordCount(covetous, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(covetous2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsDamagePlus2SkirmishingAManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var covetous = scn.GetShadowCard("covetous");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveAlliesToTable(ottar);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(covetous);
		assertEquals(0, scn.GetKeywordCount(covetous, Keyword.DAMAGE));

		// The move takes the fellowship to site 3, Ottar's home, where he can be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(ottar, covetous);
		assertEquals(0, scn.GetKeywordCount(covetous, Keyword.DAMAGE));
		scn.FreepsResolveSkirmish(ottar);

		assertEquals(2, scn.GetKeywordCount(covetous, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
