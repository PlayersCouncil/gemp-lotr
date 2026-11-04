package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herbs", "15_44");
					// Sméagol, Poor Creature: [gollum] companion, strength 3, resistance 6; to play, add a burden (cheated in);
					// his skirmish ability is not used
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, strength 3, resistance 6; to play, add a burden (cheated in);
					// an assignment ability only
					put("shiresmeagol", "103_116");
					// Sam, Son of Hamfast: abilities used only on purpose
					put("sam", "1_311");
					// Bilbo, Retired Adventurer: the ally Bilbo; his ability is used only on purpose
					put("bilbo", "1_284");
					// Merry, From O'er the Brandywine: a companion who is no Sméagol (strength 3, resistance 6 with no burden)
					put("merry", "1_303");
					// Gollum, Nasty Treacherous Creature: the minion of Sméagol's character, under another title (strength 5)
					put("gollum", "5_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herbs", "15_44");
					// Sméagol, Poor Creature: [gollum] companion, strength 3, resistance 6; to play, add a burden (cheated in);
					// his skirmish ability is not used
					put("smeagol", "6_45");
					// Frodo, Frenzied Fighter: the one Frodo who can be in play beside another Ring-bearer
					put("frodo", "13_149");
					put("sam", "1_311");
					put("bilbo", "1_284");
					// Merry, From O'er the Brandywine: a companion who is no Sméagol (strength 3, resistance 6 with no burden)
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBilboScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herbs", "15_44");
					// The Ring-bearer is Bilbo, Bearer of Things Burgled: the companion Bilbo. His move trigger is not reached.
					// Sméagol, Poor Creature: [gollum] companion, strength 3, resistance 6; to play, add a burden (cheated in);
					// his skirmish ability is not used
					put("smeagol", "6_45");
					// Merry, From O'er the Brandywine: a companion who is no Sméagol (strength 3, resistance 6 with no burden)
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				"9_49",
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herbs", "15_44");
					// The Ring-bearer is Sam, Bearer of Great Need: his regroup ability is not used
					// Bilbo, Melancholy Hobbit: the companion Bilbo; his start-of-skirmish trigger is not reached
					put("bilbo", "12_119");
					// Sméagol, Poor Creature: [gollum] companion, strength 3, resistance 6; to play, add a burden (cheated in);
					// his skirmish ability is not used
					put("smeagol", "6_45");
					// Merry, From O'er the Brandywine: a companion who is no Sméagol (strength 3, resistance 6 with no burden)
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HerbsandStewedRabbitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Herbs and Stewed Rabbit
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot Frodo, Sméagol is resistance +1.<br>While you can spot Sam, Sméagol is strength +1.<br>While you can spot Bilbo, Sméagol is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var herbs = scn.GetFreepsCard("herbs");

		assertEquals("Herbs and Stewed Rabbit", herbs.getBlueprint().getTitle());
		assertNull(herbs.getBlueprint().getSubtitle());
		assertFalse(herbs.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, herbs.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, herbs.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, herbs.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(herbs, Keyword.SUPPORT_AREA));
		assertEquals(1, herbs.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileFrodoSamAndBilboCanBeSpottedSmeagolIsResistancePlus1StrengthPlus1AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herbs = scn.GetFreepsCard("herbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, sam, merry);
		scn.MoveCardsToSupportArea(herbs, bilbo);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, herbs, bilbo);
		assertEquals(0, scn.GetBurdens());
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		// Frodo is the Ring-bearer, Sam a companion, Bilbo an ally.
		assertEquals(7, scn.GetResistance(smeagol));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		// Neither another companion nor the Gollum minion is Sméagol.
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(0, scn.GetKeywordCount(gollum, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSmeagolToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herbs = scn.GetFreepsCard("herbs");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToSupportArea(herbs, bilbo);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, herbs, bilbo);
		assertEquals(0, scn.GetBurdens());

		assertEquals(7, scn.GetResistance(smeagol));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachBonusComesOnAsItsCharacterIsPlayedAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var herbs = scn.GetFreepsCard("herbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, merry);
		scn.MoveCardsToSupportArea(herbs);
		scn.MoveCardsToHand(frodo, sam, bilbo);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, herbs);
		assertInHand(frodo, sam, bilbo);
		assertEquals(0, scn.GetBurdens());

		// Gimli is the Ring-bearer: no Frodo, Sam or Bilbo to spot. Merry is none of them.
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		scn.FreepsPlayCard(sam);
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		scn.FreepsPlayCard(bilbo);
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		// A Frodo who is not the Ring-bearer.
		scn.FreepsPlayCard(frodo);
		assertEquals(7, scn.GetResistance(smeagol));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		assertEquals(6, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BilboAsTheRingBearerMakesSmeagolDamagePlus1AndNoFrodoMeansNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBilboScenario();

		var herbs = scn.GetFreepsCard("herbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, merry);
		scn.MoveCardsToSupportArea(herbs);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, herbs);
		assertEquals(0, scn.GetBurdens());

		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SamAsTheRingBearerAndTheCompanionBilboMakeSmeagolStrengthPlus1AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var herbs = scn.GetFreepsCard("herbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, bilbo, merry);
		scn.MoveCardsToSupportArea(herbs);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, herbs);
		assertEquals(0, scn.GetBurdens());

		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
