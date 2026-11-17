package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "4_77");
					put("welcome2", "4_77");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					// Legolas, Companion of the Ring: Elf companion; stronger only at a battleground or forest site
					put("legolas", "11_21");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, damage +1; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (twice an Uruk-hai)
					put("stairs", "1_24");      // Stairs of Khazad-dum: a Free Peoples support condition; +1 only to a Dwarf against a [moria] minion

					// Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1 and nothing else
					put("raiders1", "1_158");
					put("raiders2", "1_158");
					// Cave Troll of Moria: [moria] Troll, vitality 4, damage +1 and fierce; its texts are about its play
					put("troll", "1_165");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienIsMostWelcomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lórien Is Most Welcome
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 Elves. Plays to your support area.
		 * Each wounded minion loses all damage bonuses. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("welcome");

		assertEquals("Lórien Is Most Welcome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithTwoElvesButCanOnceAnElfAllyIsPlayedAsTheThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		// Two Elf companions, and Gimli and Frodo, who are no Elves.
		scn.MoveCompanionsToTable(arwen, legolas, gimli);
		scn.MoveCardsToHand(welcome, celeborn);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(welcome, celeborn);
		assertFalse(scn.FreepsPlayAvailable(welcome));

		// An Elf ally is the third Elf.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(welcome));
		scn.FreepsPlayCard(welcome);
		assertInZone(Zone.SUPPORT, welcome);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWoundedMinionLosesItsDamageBonusAndAnUnwoundedMinionOrAWoundedCompanionKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(welcome);

		var raiders1 = scn.GetShadowCard("raiders1");
		var raiders2 = scn.GetShadowCard("raiders2");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, welcome);
		scn.MoveMinionsToTable(raiders1, raiders2, troll);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Unwounded, each has its printed damage +1.
		assertEquals(1, scn.GetKeywordCount(raiders1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// An [isengard] Uruk-hai and a [moria] Troll, wounded: neither has a damage bonus.  The other Raiding Party,
		// with no wound, keeps its own, and so does Gimli, a wounded character who is no minion.
		scn.AddWoundsToChar(raiders1, 1);
		scn.AddWoundsToChar(troll, 1);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(raiders1));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(raiders2));
		assertEquals(0, scn.GetKeywordCount(raiders1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void AMinionWoundedByAManeuverEventLosesItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCardsToHand(defend);

		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, welcome);
		scn.MoveMinionsToTable(troll);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		// Aragorn exerts to wound the Troll once (it is no Uruk-hai); he and the Troll are the only choices.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StaysThroughTheSkirmishAndIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var welcome2 = scn.GetFreepsCard("welcome2");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(welcome, welcome2, stairs);

		var raiders1 = scn.GetShadowCard("raiders1");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, welcome, welcome2, stairs);
		scn.MoveMinionsToTable(raiders1);
		scn.AddWoundsToChar(raiders1, 1);
		assertEquals(1, scn.GetWoundsOn(raiders1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders1);
		scn.FreepsResolveSkirmish(aragorn);

		// In the skirmish, the phase before regroup: both copies still in play, and the wounded Raiding Party
		// has no damage bonus.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, welcome, welcome2);
		assertEquals(0, scn.GetKeywordCount(raiders1, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// The Raiding Party (9) beats Aragorn (8) with no damage bonus: one wound, not two.  As the regroup phase
		// starts each copy is discarded; the other Free Peoples support condition stays, and the Raiding Party has
		// its damage +1 back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(welcome, welcome2);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(1, scn.GetKeywordCount(raiders1, Keyword.DAMAGE));
	}
}
