package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_111");
					put("halfling", "17_106");  // Halfling Leaf: [shire] pipeweed condition; a skirmish ability used only on purpose
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition; its trigger needs Gandalf, never in play
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession in the support area
					put("toby2", "1_305");      // a second Old Toby, kept in hand
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe on a Hobbit, which is no pipeweed

					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] unbound Hobbit, strength 3, vitality 4; his text is about tales
					// Merry, Master Holbytla: a [rohan] unbound Hobbit, strength 3; enduring (he is never wounded before he
					// is read), and his abilities are used only on purpose
					put("merry", "103_85");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer, strength 3
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is a Man

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("brigand2", "4_10");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
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
					put("leaf", "17_111");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; his abilities are used only on purpose
					put("merry", "103_85");
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is a Man
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; his assignment ability needs every
					// companion to be [shire]
					put("smeagol", "103_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,       // Gimli, Bearer of Grudges: a Ring-bearer who is a Dwarf
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthlinchLeafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Southlinch Leaf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>. To play, spot a Hobbit.<br><b>Skirmish:</b> Discard a pipeweed from play to prevent an unbound Hobbit from being overwhelmed unless that Hobbit's strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaf");

		assertEquals("Southlinch Leaf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAHobbitIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(leaf, merry);

		scn.StartGame();
		assertInHand(leaf, merry);
		// A Dwarf Ring-bearer, a Man and a [shire] companion who is no Hobbit.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(leaf));

		// A [rohan] Hobbit companion.
		scn.FreepsPlayCard(merry);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBePlayedWithAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(leaf);

		scn.StartGame();
		assertInHand(leaf);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsAPipeweedToSpareAnUnboundHobbitBeatenByDoubleAndNotOneBeatenByTriple() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(pippin, pipe);
		scn.MoveCardsToSupportArea(leaf, halfling, ship, toby);
		scn.MoveCardsToHand(toby2);
		var brigand = scn.GetShadowCard("brigand");
		var party = scn.GetShadowCard("party");
		var ted = scn.GetShadowCard("ted");
		assertInZone(Zone.SUPPORT, leaf, halfling, ship, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertInHand(toby2);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, party, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, brigand}, new PhysicalCardImpl[]{merry, party});
		// Ted is left over, and the Shadow player assigns him to nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);
		// 8 against 3: more than double, less than triple.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertInZone(Zone.SUPPORT, leaf, halfling, ship, toby);
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		// Any pipeweed in play, of either culture and either type; not the pipe, nor the pipeweed in hand.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, halfling, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pipe, toby2));
		scn.FreepsChooseCard(ship);
		assertInZone(Zone.DISCARD, ship);
		assertInZone(Zone.SUPPORT, leaf, halfling, toby);

		// The unbound Hobbit skirmishing and the one in a later skirmish, of two cultures; not the Ring-bound
		// Hobbits, the Hobbit ally, the Hobbit minion, nor an unbound companion who is no Hobbit.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, ted, aragorn));
		scn.FreepsChooseCard(pippin);
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));

		scn.PassCurrentPhaseActions();
		// Beaten and not overwhelmed: the one wound of a lost skirmish.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.FreepsResolveSkirmish(merry);
		// 9 against 3: three times.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(party));
		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(toby);
		scn.FreepsChooseCard(merry);
		assertInZone(Zone.DISCARD, toby);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		scn.PassCurrentPhaseActions();
		// Overwhelmed all the same.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheHobbitChosenNeedNotBeSkirmishingAndIsSparedOnlyForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(pippin, merry);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		assertInZone(Zone.SUPPORT, leaf, halfling);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, brigand}, new PhysicalCardImpl[]{merry, brigand2});
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(halfling);
		scn.FreepsChooseCard(merry);
		assertInZone(Zone.DISCARD, halfling);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));

		scn.PassCurrentPhaseActions();
		// Merry was spared, not Pippin: 8 against 3 overwhelms him.
		assertInZone(Zone.DEAD, pippin);

		scn.FreepsResolveSkirmish(merry);
		// Merry's own skirmish is the next phase, and the protection lasted the skirmish it was given in.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitTheSkirmishActionIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");
		assertInZone(Zone.SUPPORT, leaf, halfling);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(halfling);
		assertInZone(Zone.DISCARD, halfling);
		// Nobody to choose: no character is spared.
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertEquals(2, scn.GetOverwhelmMultiplier(aragorn));
		assertEquals(2, scn.GetOverwhelmMultiplier(bounder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var halfling = scn.GetFreepsCard("halfling");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf, halfling);
		var brigand = scn.GetShadowCard("brigand");
		assertInZone(Zone.SUPPORT, leaf, halfling);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaf));
	}
}
