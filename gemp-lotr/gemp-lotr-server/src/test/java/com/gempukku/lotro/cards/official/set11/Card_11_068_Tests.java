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

public class Card_11_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("easterling", "11_68");
					// Prowling Orc: an [orc] Orc lurker, strength 6, vitality 2.  Its skirmish ability (remove (2)
					// to make an [orc] minion strength +1) is never used.
					put("prowling", "11_136");
					// Column of Easterlings: a [men] Man lurker, strength 8, vitality 2.  Its skirmish ability
					// (discard it) is never used.
					put("column", "11_72");

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no other text
					// Ranger of Osgiliath: strength 6, vitality 3; its regroup ability is never used.
					put("osgiliath", "7_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmoredEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Armored Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Remove (2) to make a lurker strength +1.<br><b>Skirmish:</b> Remove (2) to make a lurker <b>fierce</b> until the regroup phase.<br><b>Skirmish:</b> Remove (2) to make a lurker <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("easterling");

		assertEquals("Armored Easterling", card.getBlueprint().getTitle());
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
	public void RemovesTwoToMakeALurkerStrengthPlusOneForThatSkirmishAndIsNotOfferedWithOneInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var prowling = scn.GetShadowCard("prowling");
		var column = scn.GetShadowCard("column");
		var knight = scn.GetFreepsCard("knight");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, prowling, column);
		scn.SkipToAssignments();
		// The Easterling, no lurker, fights the Knight; the Orc lurker waits for the Ranger, so that skirmish comes
		// last; the Column is left out.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, easterling}, new PhysicalCardImpl[]{osgiliath, prowling});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(easterling));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(8, scn.GetStrength(column));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(easterling, "strength"));

		scn.ShadowUseCardAction(easterling, "strength");

		// A lurker of either culture and race, assigned to a later skirmish or to none; not the Easterling, who is
		// no lurker, and no companion.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, knight, osgiliath));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(column);

		assertEquals(9, scn.GetStrength(column));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(10, scn.GetStrength(easterling));

		scn.FreepsPass();
		scn.ShadowUseCardAction(easterling, "strength");
		scn.ShadowChooseCard(prowling);
		assertEquals(1, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(prowling));
		assertEquals(9, scn.GetStrength(column));

		// One left in the pool: none of the three is offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(easterling, "strength"));
		assertFalse(scn.ShadowActionAvailable(easterling, "fierce"));
		assertFalse(scn.ShadowActionAvailable(easterling, "damage"));

		// The Knight, 6, loses to the Easterling, 10.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(knight));

		// The Orc's own skirmish is a phase of its own: both bonuses are gone.
		scn.FreepsResolveSkirmish(osgiliath);
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(8, scn.GetStrength(column));

		// With exactly 2, it is offered again, and the lurker in the skirmish being resolved is among the choices.
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(easterling, "strength"));
		scn.ShadowUseCardAction(easterling, "strength");
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, osgiliath));
		scn.ShadowChooseCard(prowling);

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(prowling));
		assertEquals(8, scn.GetStrength(column));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RemovesTwoToMakeALurkerFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var prowling = scn.GetShadowCard("prowling");
		var column = scn.GetShadowCard("column");
		var knight = scn.GetFreepsCard("knight");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, prowling, column);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, easterling}, new PhysicalCardImpl[]{osgiliath, prowling});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.HasKeyword(prowling, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(column, Keyword.FIERCE));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(easterling, "fierce"));

		scn.ShadowUseCardAction(easterling, "fierce");

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, knight, osgiliath));
		scn.ShadowChooseCard(prowling);

		assertTrue(scn.HasKeyword(prowling, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(column, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(easterling, Keyword.FIERCE));

		// The Knight, 6, loses to the Easterling, 10.
		scn.FreepsPass();
		scn.ShadowPass();

		// A later skirmish: the Orc is still fierce.  Offered again in its own skirmish, which the Orc is in.
		scn.FreepsResolveSkirmish(osgiliath);
		assertTrue(scn.HasKeyword(prowling, Keyword.FIERCE));
		scn.SetTwilight(2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(easterling, "fierce");
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, osgiliath));
		scn.ShadowChooseCard(prowling);
		assertTrue(scn.HasKeyword(prowling, Keyword.FIERCE));

		// The Ranger, 6, loses to the Orc, 6, which lives on into the fierce round and then the regroup phase.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, prowling);
		assertFalse(scn.HasKeyword(prowling, Keyword.FIERCE));
	}

	@Test
	public void RemovesTwoToMakeALurkerDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var prowling = scn.GetShadowCard("prowling");
		var column = scn.GetShadowCard("column");
		var knight = scn.GetFreepsCard("knight");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(knight, osgiliath);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, prowling, column);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, easterling}, new PhysicalCardImpl[]{osgiliath, prowling});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(column, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(prowling, Keyword.DAMAGE));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(easterling, "damage"));

		scn.ShadowUseCardAction(easterling, "damage");

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, knight, osgiliath));
		scn.ShadowChooseCard(column);

		assertEquals(1, scn.GetKeywordCount(column, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(prowling, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(easterling, Keyword.DAMAGE));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(column, Keyword.DAMAGE));
		scn.ShadowPass();

		// The next skirmish: gone.  Taken again on the Orc in it.
		scn.FreepsResolveSkirmish(osgiliath);
		assertEquals(0, scn.GetKeywordCount(column, Keyword.DAMAGE));
		scn.SetTwilight(2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(easterling, "damage");
		assertTrue(scn.ShadowHasCardChoiceAvailable(prowling, column));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, osgiliath));
		scn.ShadowChooseCard(prowling);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(prowling, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(column, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
