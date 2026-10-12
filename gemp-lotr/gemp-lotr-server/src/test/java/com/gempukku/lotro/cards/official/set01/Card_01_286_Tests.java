package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_286_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bounder", "1_286");
					put("bounder2", "1_286");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, strength 3, vitality 4; his text is about tales
					// Sam, Son of Hamfast: [shire] Hobbit, strength 3, vitality 4; a fellowship ability and a response
					// to Frodo's death, neither of which comes up
					put("sam", "1_311");
					// Merry, Master Holbytla: a [rohan] Hobbit, strength 3; enduring and valiant, and a Man only beside
					// Eowyn or 3 [rohan] companions; his abilities are used only on purpose
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is a Man
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; his assignment ability needs every
					// companion to be [shire]
					put("smeagol", "103_116");

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1
					// Uruk Seeker: fierce, strength 6; stronger only against a bearer of a search card
					put("seeker", "4_195");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on
					// purpose
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BounderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bounder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 2F
		 * Game Text: <b>Skirmish:</b> Exert this ally to prevent a Hobbit from being overwhelmed unless that Hobbit's strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bounder");

		assertEquals("Bounder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 2)));
	}

	@Test
	public void ExertsToKeepAHobbitBeatenByDoubleButUnderTripleFromBeingOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, brigand);
		scn.FreepsResolveSkirmish(pippin);

		// 8 against 3: more than double, less than triple.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(bounder));

		scn.FreepsUseCardAction(bounder);
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));

		scn.ShadowPass();
		scn.FreepsPass();

		// Beaten and not overwhelmed: the one wound of a lost skirmish.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyHobbitMayBeChosenAndOnlyTheOneChosenIsProtected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, merry, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);

		var brigand = scn.GetShadowCard("brigand");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, ted);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, brigand);
		// Ted is left over, and the Shadow player assigns him to nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsUseCardAction(bounder);

		// Every Hobbit: the one skirmishing, a [rohan] one in no skirmish, the Ring-bearer, Bounder himself, and
		// the other player's Hobbit minion. Not a companion who is a Man, nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));

		scn.ShadowPass();
		scn.FreepsPass();

		// Merry was protected, not Pippin: 8 against 3 overwhelms him.
		assertInZone(Zone.DEAD, pippin);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ProtectsOneShortOfTripleAndNotAtTriple() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var bounder2 = scn.GetFreepsCard("bounder2");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin, sam);
		scn.MoveAlliesToTable(bounder, bounder2);

		var brigand = scn.GetShadowCard("brigand");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, brigand}, new PhysicalCardImpl[]{sam, party});

		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(brigand));
		scn.FreepsUseCardAction(bounder);
		scn.FreepsChooseCard(pippin);
		// The copy used is the one that exerts.
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(bounder2));
		scn.ShadowPass();
		scn.FreepsPass();

		// 8 is under three times 3: beaten and wounded.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(party));
		assertFalse(scn.FreepsActionAvailable(bounder));
		scn.FreepsUseCardAction(bounder2);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(bounder2));
		assertEquals(3, scn.GetOverwhelmMultiplier(sam));
		scn.ShadowPass();
		scn.FreepsPass();

		// 9 is three times 3: overwhelmed all the same.
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedBounderCannotExertAndAnotherCopyStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var bounder2 = scn.GetFreepsCard("bounder2");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder, bounder2);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(bounder, 1);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, brigand);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bounder));
		assertTrue(scn.FreepsActionAvailable(bounder2));
	}

	@Test
	public void TheProtectionEndsWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(seeker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, seeker);
		scn.FreepsResolveSkirmish(pippin);

		// 6 against 3: double.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(seeker));
		scn.FreepsUseCardAction(bounder);
		scn.FreepsChooseCard(pippin);
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));
		scn.ShadowPass();
		scn.FreepsPass();

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));

		// The fierce round: the same Pippin against the same Seeker, with Bounder exhausted.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, seeker);
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bounder));
		scn.PassFierceSkirmishActions();

		// Still protected he would have a second wound and be alive.
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
