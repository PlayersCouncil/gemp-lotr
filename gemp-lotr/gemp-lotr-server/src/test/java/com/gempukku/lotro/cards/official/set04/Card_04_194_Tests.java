package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("searcher", "4_194");
					put("searcher2", "4_194");
					put("searcher3", "4_194");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					put("rest", "4_171");       // Rest While You Can: [isengard] search condition on a companion; answers its bearer killed
					put("weary", "4_212");      // Weary: [isengard] search condition on a companion or ally; its bearer exerts as he is assigned to an [isengard] tracker
					put("voice", "1_142");      // Traitor's Voice: [isengard] search condition in the support area; a move-limit text that needs 5 burdens or an exhausted Ring-bearer
					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears, no search card; its bearer exerts as he is assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, vitality 2, who may exert to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSearcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Searcher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.<br>While skirmishing a character bearing a search card, this minion is strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("searcher");

		assertEquals("Uruk Searcher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus2AndDamagePlus1OnlyWhileSkirmishingACompanionBearingASearchCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var searcher = scn.GetShadowCard("searcher");
		var searcher2 = scn.GetShadowCard("searcher2");
		var searcher3 = scn.GetShadowCard("searcher3");
		var savage = scn.GetShadowCard("savage");
		var rest = scn.GetShadowCard("rest");
		var voice = scn.GetShadowCard("voice");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Aragorn bears a search card; Gimli a Shadow card that is none.  A search card that nobody bears is in
		// the Shadow player's support area.
		scn.AttachCardsTo(aragorn, rest);
		scn.AttachCardsTo(gimli, poison);
		scn.MoveCardsToSupportArea(voice);
		assertAttachedTo(rest, aragorn);
		assertAttachedTo(poison, gimli);
		assertInZone(Zone.SUPPORT, voice);

		scn.StartGame();
		scn.MoveMinionsToTable(searcher, searcher2, searcher3, savage);
		scn.SkipToAssignments();

		assertEquals(9, scn.GetStrength(searcher));
		assertEquals(0, scn.GetKeywordCount(searcher, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, searcher}, new PhysicalCardImpl[]{aragorn, searcher2});
		scn.ShadowAssignToMinions(aragorn, searcher3, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Skirmishing Gimli, who bears a Shadow card but no search card.
		assertEquals(9, scn.GetStrength(searcher));
		assertEquals(0, scn.GetKeywordCount(searcher, Keyword.DAMAGE));
		// Assigned to Aragorn, who bears one, but not skirmishing him yet.
		assertEquals(9, scn.GetStrength(searcher2));
		assertEquals(0, scn.GetKeywordCount(searcher2, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(searcher3));
		assertEquals(0, scn.GetKeywordCount(searcher3, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Skirmishing Aragorn, who bears Rest While You Can: 9 + 2 and damage +1 each, and two copies in the one
		// skirmish do not give each other theirs.
		assertEquals(11, scn.GetStrength(searcher2));
		assertEquals(1, scn.GetKeywordCount(searcher2, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(searcher3));
		assertEquals(1, scn.GetKeywordCount(searcher3, Keyword.DAMAGE));
		// Another Uruk-hai in the same skirmish is not "this minion": its printed 5 and damage +1.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		// The copy whose skirmish is over is in none now.
		assertEquals(9, scn.GetStrength(searcher));
		assertEquals(0, scn.GetKeywordCount(searcher, Keyword.DAMAGE));
	}

	@Test
	public void StrengthPlus2AndDamagePlus1WhileSkirmishingAnAllyBearingASearchCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var searcher = scn.GetShadowCard("searcher");
		var weary = scn.GetShadowCard("weary");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, weary);
		assertAttachedTo(weary, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(searcher);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, searcher);
		assertEquals(9, scn.GetStrength(searcher));
		assertEquals(0, scn.GetKeywordCount(searcher, Keyword.DAMAGE));
		scn.FreepsResolveSkirmish(thrarin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 9 + 2
		assertEquals(11, scn.GetStrength(searcher));
		assertEquals(1, scn.GetKeywordCount(searcher, Keyword.DAMAGE));
	}
}
