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

public class Card_11_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lathspell", "11_87");

					// Axeman of Harad: a [men] Man minion, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					put("axeman2", "11_69");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, cost 1, site 2, strength 3,
					// vitality 4.  His maneuver and regroup abilities are never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Harmless: a [men] condition in the support area.  Its response is to a lost skirmish, never reached
					// while it is on the table.
					put("harmless", "13_93");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Ranger's Sword: a possession, strength +2, "He is damage +1".  On Aragorn.
					put("sword", "1_112");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, printed damage +1.  "Skirmish: Exert Gimli to
					// make him strength +2": a strength bonus that is not from a possession.
					put("gimli", "1_13");
					// Dwarven Axe: a possession, strength +2.  Its trigger on a minion losing to its bearer only mills
					// the Shadow player's deck.
					put("axe", "1_9");
					// Ring of Accretion: an ARTIFACT, strength +1 and "Bearer is damage +1".  Its maneuver ability is never used.
					put("ring", "9_6");
					// Barliman Butterbur: a [gandalf] Man ally.  His fellowship ability is never used.
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LathspellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Láthspell
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [men] minion.
		 * 		Bearer must be a companion.
		 * 		While bearer is skirmishing a [men] minion, bearer loses all strength and <b>damage</b> bonuses from possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lathspell");

		assertEquals("Láthspell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMenMinionIsSpottedAndThenGoesOnACompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		var lathspell = scn.GetShadowCard("lathspell");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var harmless = scn.GetShadowCard("harmless");

		scn.StartGame();
		scn.SetShadowHand(lathspell, ted);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToSupportArea(harmless);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInZone(Zone.SUPPORT, harmless);
		assertInHand(lathspell);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [dunland] Man minion and a [men] condition: no [men] minion to spot.
		assertFalse(scn.ShadowPlayAvailable(lathspell));

		// Ted Sandyman, a [men] minion who is a Hobbit, played from hand at his own site 2.
		scn.ShadowPlayCard(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertTrue(scn.ShadowPlayAvailable(lathspell));

		scn.ShadowPlayCard(lathspell);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman, ted, brigand));

		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(lathspell, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedWithAMenManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lathspell = scn.GetShadowCard("lathspell");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SetShadowHand(lathspell);
		scn.MoveMinionsToTable(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(lathspell));

		scn.ShadowPlayCard(lathspell);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));

		scn.ShadowChooseCard(frodo);
		assertAttachedTo(lathspell, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABearerSkirmishingAMenMinionLosesTheStrengthAndDamageBonusesOfHisPossessionsUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe);

		var lathspell = scn.GetShadowCard("lathspell");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.AttachCardsTo(aragorn, lathspell);

		assertAttachedTo(sword, aragorn);
		assertAttachedTo(lathspell, aragorn);
		assertAttachedTo(axe, gimli);
		// 8 + 2 from the Sword, and the Sword's damage +1.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// 6 + 2 from the Axe.
		assertEquals(8, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, axeman},
				new PhysicalCardImpl[]{gimli, axeman2});

		// Assigned to the Axeman, Aragorn is not yet skirmishing him.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(gimli);

		// Gimli bears no Láthspell, and keeps the Axe's +2 against a [men] minion.  Aragorn is still waiting.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// The other Axeman's 11 beats Gimli's 8: one wound, and he lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Axeman's 11 beats Aragorn's 8: one wound, and he lives.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// No longer skirmishing, he has the Sword's bonuses again.
		assertAttachedTo(lathspell, aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void ABearerSkirmishingAMinionOfAnotherCultureKeepsHisBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var lathspell = scn.GetShadowCard("lathspell");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(aragorn, lathspell);

		assertAttachedTo(sword, aragorn);
		assertAttachedTo(lathspell, aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);

		// A [dunland] Man is no [men] minion.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void OnlyTheBonusesOfPossessionsAreLostAgainstAMenMinionWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, ring);

		var lathspell = scn.GetShadowCard("lathspell");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(gimli, lathspell);

		assertAttachedTo(axe, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(lathspell, gimli);
		// 6 + 2 from the Axe + 1 from the Ring; damage +1 printed and +1 from the Ring.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, ted);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Skirmishing a [men] Hobbit: the Axe's +2 is gone; the Ring is an artifact, and its +1 and damage +1 stay,
		// as does his printed damage +1.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// A strength bonus that is not from a possession is not lost.
		scn.FreepsUseCardAction(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
