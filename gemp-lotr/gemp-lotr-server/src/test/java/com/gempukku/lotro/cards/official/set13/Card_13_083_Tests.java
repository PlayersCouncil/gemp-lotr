package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_083_Tests
{

	// A Shadows-block path, on which whoever plays a site chooses it from his adventure deck.  The sites these tests
	// lay: Fangorn Glade (a forest: twilight for Free Peoples weapons when the fellowship moves to it), Crags of Emyn
	// Muil (a mountain: its text is about region 2), Nurn (a plains: its text is about a site its owner controls),
	// Sirannon Ruins (a river, no text), and two put in for this card, Northern Pelennor (a battleground and a
	// plains: a Shadow ability used only on purpose) and Mere of Dead Faces (a marsh: "Regroup: Exert your Gollum or
	// your Smeagol to play the fellowship's next site").
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("caravan", "13_83");
					put("caravan2", "13_83");
					// Easterling Runner: [men] Man; a Shadow ability used only on purpose
					put("runner", "13_90");
					// Saruman, Coldly Still: a [men] minion who is a Wizard, cost 4, site number 4.  "Each [men]
					// minion bearing a possession is strength +2"; his skirmish ability is never used here.
					put("saruman", "17_48");
					put("brigand", "4_10");       // Dunlending Brigand: a Man of the [dunland] culture, no text
					// Stragglers: [men] support-area condition, not unique.  "When you play this, add a [men] token
					// here."  Its skirmish ability is never used here.
					put("stragglers", "13_99");
					// Fleet of Corsair Ships: [men] support-area possession.  "When you play this possession, you
					// may add a [men] token here."  Its regroup ability is never used here.
					put("fleet", "18_66");
					// Howdah: [men] support-area condition; a token for each [men] archer as it is played.  Here it
					// has none.
					put("howdah", "13_94");
					// Ships of Great Draught: [raider] support-area possession that takes [raider] tokens by its
					// own Shadow ability.
					put("ships", "8_65");
					put("bowmen", "1_264");       // Orc Bowmen: [sauron] support-area condition, inert without a [sauron] Orc
					// Corsair Boarding Axe: [men] hand weapon borne by a [men] minion; its skirmish ability is never used
					put("axe", "18_60");

					put("lordOfMoria", "1_21");   // Lord of Moria: [dwarven] support-area condition, inert without a Dwarf
					put("toby", "1_305");         // Old Toby: a [shire] possession in the support area; its text is about being played
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion to bear the tale and Narsil
					put("saga", "1_114");         // The Saga of Elendil: [gondor] condition borne by a [gondor] companion
					// Narsil, Blade of the Faithful: a [gondor] artifact, a hand weapon, on a [gondor] Man; its
					// response is never used here.
					put("narsil", "9_34");
					// Glamdring: a [gandalf] possession, a hand weapon, on Gandalf; its ability is never used here.
					put("glamdring", "1_75");
					// Speak "Friend" and Enter: "Fellowship or Regroup: Spot Gandalf to play the fellowship's next
					// site.  Draw a card if you play an underground site."  No site played here is underground.
					put("speak", "2_26");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk
					put("smeagol", "5_29");       // Smeagol, Slinker: his skirmish ability is never used here
					// There's Another Way: [gollum] support-area condition.  "Smeagol is strength +1.  Regroup:
					// Discard this condition to replace the fellowship's current site with a site from your
					// adventure deck."
					put("way", "12_40");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site8", "12_190");       // Northern Pelennor
					put("site9", "11_246");       // Mere of Dead Faces
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void CaravanFromtheSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Caravan From the South
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [men] minion.<br>When you play this, if you spot a plains site on the adventure path, reinforce 3 [men] tokens.<br>Each time the Free Peoples player plays the fellowship's next site, you may discard a possession from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("caravan");

		assertEquals("Caravan From the South", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMenCultureMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caravan = scn.GetShadowCard("caravan");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(caravan, saruman);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		// A Man who is not of the [men] culture, and a [men] card that is no minion.
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(caravan));

		// Saruman is a [men] minion and no Man.
		scn.ShadowPlayCard(saruman);
		assertTrue(scn.ShadowPlayAvailable(caravan));

		scn.ShadowPlayCard(caravan);
		assertInZone(Zone.SUPPORT, caravan);
		assertNotAttachedTo(caravan, saruman);
		// Neither site on the path is a plains: nothing was reinforced.
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingWithPlainsOnAdventurePathReinforces3MenTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caravan = scn.GetShadowCard("caravan");
		var caravan2 = scn.GetShadowCard("caravan2");
		var runner = scn.GetShadowCard("runner");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var howdah = scn.GetShadowCard("howdah");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(caravan);

		// The fellowship starts at Northern Pelennor and moves on to Sirannon Ruins: the plains lies behind it.
		var pelennor = scn.GetFreepsSite("site8");
		scn.StartGame(pelennor);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.MoveMinionsToTable(runner);
		// A [men] condition and a [men] possession with a [men] token each, one with none, a card with a [raider]
		// token, and another copy of the card itself.
		scn.MoveCardsToSupportArea(stragglers, fleet, howdah, ships, caravan2);
		scn.AddTokensToCard(stragglers, 1);
		scn.AddTokensToCard(fleet, 1);
		scn.AddTokensToCard(ships, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.PLAINS));
		assertInZone(Zone.ADVENTURE_PATH, pelennor);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		scn.ShadowPlayCard(caravan);

		// No "may": the first of three, on a card that already has a [men] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(howdah, ships, caravan, caravan2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stragglers);
		assertEquals(2, scn.GetCultureTokensOn(stragglers));

		// The second may go to the same card ...
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		scn.ShadowChooseCard(stragglers);

		// ... and the third to another.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		scn.ShadowChooseCard(fleet);

		assertEquals(3, scn.GetCultureTokensOn(stragglers));
		assertEquals(2, scn.GetCultureTokensOn(fleet));
		assertEquals(0, scn.GetCultureTokensOn(howdah));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(caravan));
		// The copy that was already in play was not played: three tokens in all, and nothing more is asked.
		assertEquals(0, scn.GetCultureTokensOn(caravan2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReinforcesWhereThePlainsIsTheSiteTheFellowshipIsAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caravan = scn.GetShadowCard("caravan");
		var runner = scn.GetShadowCard("runner");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(caravan);

		// From Fangorn Glade to the Shadow player's Nurn, a plains and nothing else.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site4"));
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.PLAINS));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));

		scn.ShadowPlayCard(caravan);

		// One card has a token: all three go on it, with nothing to choose.
		assertEquals(4, scn.GetCultureTokensOn(stragglers));
		assertEquals(0, scn.GetCultureTokensOn(caravan));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingWithoutPlainsOnAdventurePathDoesNotReinforce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caravan = scn.GetShadowCard("caravan");
		var runner = scn.GetShadowCard("runner");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(caravan);

		// From Fangorn Glade to Sirannon Ruins.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.PLAINS));
		// Each player's Nurn and Northern Pelennor are in his adventure deck: plains, and not on the adventure path.
		assertInZone(Zone.ADVENTURE_DECK, scn.GetFreepsSite("site4"), scn.GetShadowSite("site4"),
				scn.GetFreepsSite("site8"), scn.GetShadowSite("site8"));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));

		scn.ShadowPlayCard(caravan);

		assertInZone(Zone.SUPPORT, caravan);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APlainsSiteTheShadowPlayerControlsIsNotOnTheAdventurePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caravan = scn.GetShadowCard("caravan");
		var runner = scn.GetShadowCard("runner");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToHand(caravan);

		// The first player's first turn takes him from Northern Pelennor to Sirannon Ruins, and the second player's
		// turn takes that fellowship there too.
		var pelennor = scn.GetFreepsSite("site8");
		scn.StartGame(pelennor);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Both markers are past Northern Pelennor: the second player takes control of it.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		scn.ShadowTakeControlOfSite(pelennor);
		assertTrue(scn.ShadowControls(pelennor));

		// On to Crags of Emyn Muil.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Still a plains, and in his support area (CR 4.2 "control": "no longer a site on the adventure path").
		assertTrue(scn.HasKeyword(pelennor, Keyword.PLAINS));
		assertInZone(Zone.SUPPORT, pelennor);
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));

		scn.ShadowPlayCard(caravan);

		assertInZone(Zone.SUPPORT, caravan);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteLetsShadowDiscardAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var narsil = scn.GetFreepsCard("narsil");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(aragorn, saga, narsil);
		scn.MoveCardsToSupportArea(lordOfMoria, toby);

		var caravan = scn.GetShadowCard("caravan");
		var bowmen = scn.GetShadowCard("bowmen");
		var ships = scn.GetShadowCard("ships");

		var crags = scn.GetFreepsSite("site3");
		scn.StartGame(scn.GetFreepsSite("site1"));
		// The second player's cards, as an earlier turn of his would have left them.
		scn.MoveCardsToSupportArea(caravan, bowmen, ships);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.ADVENTURE_DECK, crags);
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, lordOfMoria, toby, caravan, bowmen, ships);

		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertInZone(Zone.ADVENTURE_PATH, crags);
		assertSame(crags, scn.GetSite(2));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan));
		scn.ShadowAcceptOptionalTrigger();

		// A possession of either side, in a support area or borne.  A condition of either side (the card itself
		// among them) is none, and nor is an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(glamdring, toby, ships));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, saga, lordOfMoria, bowmen, caravan));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(glamdring);

		assertInDiscard(glamdring);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, bowmen, caravan, ships, toby, lordOfMoria);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteTriggerIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(toby);

		var caravan = scn.GetShadowCard("caravan");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(caravan);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.SUPPORT, caravan);
		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(scn.GetFreepsSite("site3"));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, toby, caravan);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToSupportArea(lordOfMoria);

		var caravan = scn.GetShadowCard("caravan");
		var bowmen = scn.GetShadowCard("bowmen");

		var crags = scn.GetFreepsSite("site3");
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(caravan, bowmen);
		scn.FreepsRefreshDecision();

		// Conditions and an artifact, and no possession, in play: there is nothing to discard.
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, lordOfMoria, caravan, bowmen);
		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertSame(crags, scn.GetSite(2));

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertAttachedTo(narsil, aragorn);
		assertInZone(Zone.SUPPORT, lordOfMoria, caravan, bowmen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowPlayingNextSiteDuringMovementDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		var caravan = scn.GetShadowCard("caravan");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(caravan);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.SUPPORT, caravan);

		// The move: the Shadow player plays the fellowship's next site, as the rules have it.
		scn.FreepsPass();
		var ruins = scn.GetShadowSite("site5");
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertSame(ruins, scn.GetCurrentSite());

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, toby, caravan);
	}

	@Test
	public void EachCopyOffersEachTimeAndASitesOwnTextPlayingTheNextSiteIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var smeagol = scn.GetFreepsCard("smeagol");
		var glamdring = scn.GetFreepsCard("glamdring");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf, smeagol);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(toby);

		var caravan = scn.GetShadowCard("caravan");
		var caravan2 = scn.GetShadowCard("caravan2");
		var runner = scn.GetShadowCard("runner");
		var axe = scn.GetShadowCard("axe");

		var mere = scn.GetFreepsSite("site9");
		var crags = scn.GetFreepsSite("site3");
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(caravan, caravan2);
		scn.FreepsRefreshDecision();

		// The fellowship phase: Speak "Friend" and Enter lays Mere of Dead Faces as site 2.  Each copy offers.
		assertInZone(Zone.SUPPORT, toby);
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, caravan, caravan2);
		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(mere);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan2));
		scn.ShadowChooseOptionalTrigger(caravan);
		assertTrue(scn.ShadowHasCardChoiceAvailable(toby, glamdring));
		scn.ShadowChooseCard(toby);
		assertInDiscard(toby);
		// The other copy's offer is still to be answered, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan2));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, caravan, caravan2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves to the site already there: nobody plays one, and nothing is offered.
		scn.FreepsPass();
		assertSame(mere, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The regroup phase, with a minion that lived through the skirmishes bearing a Shadow possession.  By the
		// site's own text Smeagol exerts and the Free Peoples player lays site 3.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, axe);
		scn.FreepsRefreshDecision();
		assertAttachedTo(axe, runner);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsUseCardAction(mere);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(crags, scn.GetSite(3));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan2));
		scn.ShadowChooseOptionalTrigger(caravan2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, glamdring));
		scn.ShadowChooseCard(axe);
		assertInDiscard(axe);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(caravan));
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.SUPPORT, caravan, caravan2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerReplacesTheSiteTheFellowshipIsAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var way = scn.GetFreepsCard("way");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(way, toby);

		var caravan = scn.GetShadowCard("caravan");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(caravan);
		assertInZone(Zone.SUPPORT, way, toby, caravan);
		scn.FreepsPass();
		var ruins = scn.GetShadowSite("site5");
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToPhase(Phase.REGROUP);
		assertSame(ruins, scn.GetCurrentSite());

		// There's Another Way: the Free Peoples player plays a site of his own, and it is site 2, where the
		// fellowship is, not the next.
		var crags = scn.GetFreepsSite("site3");
		scn.FreepsUseCardAction(way);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertInDiscard(way);
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, caravan, toby);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
